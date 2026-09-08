import os
import re
import uuid
import logging
from datetime import datetime
from ftplib import FTP, error_perm
from fastapi import UploadFile, HTTPException, status
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
        ext = os.path.splitext(file.filename)[1].lower() if file.filename else ""
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
    def _makedirs_ftp(ftp: FTP, remote_path: str):
        """
        Crea directorios remotos recursivamente en el servidor FTP.
        """
        parts = [p for p in remote_path.strip("/").split("/") if p]
        
        try:
            ftp.cwd("/")
        except Exception as e:
            logger.error(f"Error accediendo a la raíz del FTP: {str(e)}")

        current_dir = ""
        for part in parts:
            current_dir = f"{current_dir}/{part}"
            try:
                ftp.cwd(current_dir)
            except error_perm:
                try:
                    ftp.mkd(current_dir)
                    ftp.cwd(current_dir)
                    logger.info(f"Directorio remoto FTP creado: {current_dir}")
                except Exception as e:
                    logger.error(f"Error creando directorio FTP {current_dir}: {str(e)}")
                    raise e

    @classmethod
    def upload_photo(
        cls,
        file: UploadFile,
        user_code: str,
        client_ruc: str
    ) -> str:
        """
        Sube un archivo de imagen al servidor Plesk por FTP estándar (sin SSL/certificados).
        Retorna la ruta relativa del archivo guardado (ej. '/user_code/client_ruc/filename.jpg').
        """
        # 1. Validar el archivo
        cls.validate_file(file)

        # 2. Sanitizar segmentos de ruta
        safe_user = cls.sanitize_path_segment(user_code)
        safe_ruc = cls.sanitize_path_segment(client_ruc)

        # 3. Generar nombre de archivo único
        ext = os.path.splitext(file.filename)[1].lower() if file.filename else ".jpg"
        now_str = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        filename = f"{now_str}{ext}"

        # Obtener datos de conexión FTP desde settings (FTP_* con fallback a SFTP_*)
        ftp_host = getattr(settings, "FTP_HOST", None) or settings.SFTP_HOST
        ftp_port = getattr(settings, "FTP_PORT", None) if getattr(settings, "FTP_HOST", None) else (21 if settings.SFTP_PORT == 22 else settings.SFTP_PORT)
        ftp_username = getattr(settings, "FTP_USERNAME", None) or settings.SFTP_USERNAME
        ftp_password = getattr(settings, "FTP_PASSWORD", None) or settings.SFTP_PASSPHRASE
        base_remote_dir = getattr(settings, "FTP_REMOTE_DIR", None) or settings.SFTP_REMOTE_DIR

        ftp = FTP()
        try:
            logger.info(f"Conectando al servidor FTP {ftp_host}:{ftp_port}...")
            ftp.connect(host=ftp_host, port=ftp_port, timeout=15)
            logger.info(f"Iniciando sesión en FTP con usuario '{ftp_username}'...")
            ftp.login(user=ftp_username, passwd=ftp_password)
        except Exception as e:
            logger.error(f"Error conectando o autenticando en FTP: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="No se pudo establecer conexión FTP con el servidor de almacenamiento."
            )

        try:
            # 4. Crear la ruta remota
            # Carpeta remota: {base_remote_dir}/{safe_user}/{safe_ruc}
            remote_dir = f"{base_remote_dir.rstrip('/')}/{safe_user}/{safe_ruc}"
            cls._makedirs_ftp(ftp, remote_dir)

            # 5. Prevenir colisiones de nombre de archivo en el directorio de destino
            try:
                existing_files = set(ftp.nlst())
            except Exception:
                existing_files = set()

            while filename in existing_files:
                filename = f"{now_str}_{uuid.uuid4().hex[:8]}{ext}"

            # 6. Subir archivo mediante STOR
            file.file.seek(0)
            logger.info(f"Subiendo archivo por FTP a: {remote_dir}/{filename}")
            ftp.storbinary(f"STOR {filename}", file.file)
            logger.info("Subida FTP exitosa.")

            # 7. Retornar ruta relativa de la imagen (ej. /ADM001/20601122334/2026-07-01_14-35-21.jpg)
            relative_path = f"/{safe_user}/{safe_ruc}/{filename}"
            return relative_path

        except Exception as e:
            logger.error(f"Error durante la subida FTP del archivo: {str(e)}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Error al guardar el archivo en el servidor remoto mediante FTP."
            )
        finally:
            try:
                ftp.quit()
            except Exception:
                try:
                    ftp.close()
                except Exception:
                    pass
            logger.info("Conexión FTP cerrada.")
