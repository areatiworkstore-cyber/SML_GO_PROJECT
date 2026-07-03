import os
import re
import uuid
import logging
from datetime import datetime
from fastapi import UploadFile, HTTPException, status
import paramiko
from app.core.config import settings

logger = logging.getLogger(__name__)

# Configuración y límites de seguridad
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10 MB
ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}
ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/webp"}

class MediaStorageService:
    @staticmethod
    def sanitize_path_segment(segment: str) -> str:
        """
        Sanitiza un segmento de ruta para evitar Path Traversal y caracteres no permitidos.
        Solo permite caracteres alfanuméricos, guiones y guiones bajos.
        """
        if not segment:
            return "desconocido"
        # Eliminar cualquier intento de navegación de directorios (../, ..\\)
        segment = os.path.basename(segment)
        # Reemplazar caracteres no permitidos
        sanitized = re.sub(r"[^a-zA-Z0-9_\-]", "", segment)
        return sanitized if sanitized else "desconocido"

    @staticmethod
    def validate_file(file: UploadFile):
        """
        Valida que el archivo subido sea una imagen permitida y no exceda el tamaño límite.
        """
        # 1. Validar extensión
        ext = os.path.splitext(file.filename)[1].lower()
        if ext not in ALLOWED_EXTENSIONS:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Extensión de archivo '{ext}' no soportada. Permitidas: {', '.join(ALLOWED_EXTENSIONS)}"
            )

        # 2. Validar Content-Type (MIME type)
        if file.content_type not in ALLOWED_MIME_TYPES:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Tipo MIME '{file.content_type}' no permitido. Permitidos: {', '.join(ALLOWED_MIME_TYPES)}"
            )

        # 3. Validar tamaño de archivo leyendo el cursor y volviendo al inicio
        file.file.seek(0, os.SEEK_END)
        size = file.file.tell()
        file.file.seek(0)  # Resetear cursor al inicio para la subida

        if size > MAX_FILE_SIZE:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"El tamaño del archivo ({size / (1024*1024):.2f} MB) excede el límite permitido de {MAX_FILE_SIZE / (1024*1024)} MB."
            )

    @staticmethod
    def _makedirs_sftp(sftp: paramiko.SFTPClient, remote_path: str):
        """
        Crea directorios remotos recursivamente en el servidor SFTP
        y les asigna permisos 755 (rwxr-xr-x).
        """
        parts = remote_path.strip("/").split("/")
        current = ""
        if remote_path.startswith("/"):
            current = "/"
        
        for part in parts:
            if not part:
                continue
            current = os.path.join(current, part).replace("\\", "/")
            try:
                sftp.stat(current)
            except IOError:
                try:
                    sftp.mkdir(current)
                    sftp.chmod(current, 0o755)
                    logger.info(f"Directorio remoto creado: {current}")
                except Exception as e:
                    logger.error(f"Error creando directorio SFTP {current}: {str(e)}")
                    raise e

    @classmethod
    def upload_photo(
        cls,
        file: UploadFile,
        user_code: str,
        client_ruc: str
    ) -> str:
        """
        Sube un archivo de imagen al servidor cPanel por SFTP.
        Retorna la ruta relativa del archivo guardado (ej. '/user_code/client_ruc/filename.jpg').
        """
        # 1. Validar el archivo
        cls.validate_file(file)

        # 2. Sanitizar segmentos de ruta
        safe_user = cls.sanitize_path_segment(user_code)
        safe_ruc = cls.sanitize_path_segment(client_ruc)

        # 3. Generar nombre de archivo único
        ext = os.path.splitext(file.filename)[1].lower()
        now_str = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        filename = f"{now_str}{ext}"

        # 4. Establecer conexión SFTP con clave privada
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        # Ruta absoluta al archivo cert/id_sml_vps
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        key_path = os.path.join(base_dir, "certs", "id_sml_vps")

        if not os.path.exists(key_path):
            logger.error(f"Clave SSH no encontrada en la ruta: {key_path}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Error de configuración del sistema de almacenamiento."
            )

        try:
            logger.info("Cargando clave privada SSH...")
            pkey = paramiko.Ed25519Key.from_private_key_file(
                key_path,
                password=settings.SFTP_PASSPHRASE if settings.SFTP_PASSPHRASE else None
            )

            logger.info(f"Conectando a servidor SFTP {settings.SFTP_HOST}...")
            ssh.connect(
                hostname=settings.SFTP_HOST,
                port=settings.SFTP_PORT,
                username=settings.SFTP_USERNAME,
                pkey=pkey,
                timeout=15
            )

            sftp = ssh.open_sftp()
        except paramiko.PasswordRequiredException:
            logger.error("La clave SSH requiere passphrase y no se proporcionó o es incorrecta.")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Fallo de autenticación del sistema de almacenamiento."
            )
        except Exception as e:
            logger.error(f"Error conectando al SFTP: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="No se pudo establecer conexión con el servidor de almacenamiento."
            )

        try:
            # 5. Crear la ruta remota
            # Carpeta remota: {SFTP_REMOTE_DIR}/{safe_user}/{safe_ruc}
            remote_dir = os.path.join(settings.SFTP_REMOTE_DIR, safe_user, safe_ruc).replace("\\", "/")
            cls._makedirs_sftp(sftp, remote_dir)

            # 6. Prevenir colisiones de nombre de archivo
            remote_file_path = os.path.join(remote_dir, filename).replace("\\", "/")
            collision_count = 0
            while True:
                try:
                    sftp.stat(remote_file_path)
                    # Colisión encontrada -> Modificar nombre
                    collision_count += 1
                    filename = f"{now_str}_{uuid.uuid4().hex[:8]}{ext}"
                    remote_file_path = os.path.join(remote_dir, filename).replace("\\", "/")
                except IOError:
                    # No existe el archivo -> Ruta libre
                    break

            # 7. Subir archivo
            logger.info(f"Subiendo archivo a: {remote_file_path}")
            sftp.putfo(file.file, remote_file_path)

            # Permisos 644 (rw-r--r--) para el archivo remoto
            sftp.chmod(remote_file_path, 0o644)
            logger.info("Subida exitosa y permisos aplicados.")

            # 8. Retornar ruta relativa de la imagen
            # Guardamos con el prefijo '/' para facilitar la construcción de URLs públicas
            relative_path = f"/{safe_user}/{safe_ruc}/{filename}"
            return relative_path

        except Exception as e:
            logger.error(f"Error durante la subida SFTP del archivo: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Error al guardar el archivo en el servidor remoto."
            )
        finally:
            if 'sftp' in locals():
                sftp.close()
            ssh.close()
            logger.info("Conexión SFTP cerrada.")
