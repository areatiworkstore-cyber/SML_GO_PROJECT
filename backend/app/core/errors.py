import re
from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from sqlalchemy.exc import IntegrityError

def init_error_handlers(app: FastAPI) -> None:
    """
    Inicializa los manejadores de errores globales para la aplicación FastAPI.
    """

    @app.exception_handler(IntegrityError)
    async def sqlalchemy_integrity_error_handler(request: Request, exc: IntegrityError):
        # El error original de PyMySQL está en exc.orig
        error_msg = str(exc.orig)
        
        # 1062 es el código de error de MySQL/MariaDB para entradas duplicadas (Duplicate Entry)
        if "1062" in error_msg:
            # Intentamos extraer qué campo y qué valor están duplicados usando regex
            # Ejemplo: "Duplicate entry '72657497' for key 'document_number'"
            match = re.search(r"Duplicate entry '([^']+)' for key '([^']+)'", error_msg)
            
            if match:
                value = match.group(1)
                field = match.group(2)
                
                # Customizar el nombre si viene con sufijos raros del index de la base de datos
                friendly_field = "Número de documento" if "document_number" in field else field
                
                return JSONResponse(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    content={
                        "detail": f"Ya existe un registro con el código o número de documento '{value}'."
                    }
                )
            
            return JSONResponse(
                status_code=status.HTTP_400_BAD_REQUEST,
                content={"detail": "Registro duplicado en la base de datos."}
            )

        # Para otros errores de integridad (llaves foráneas que no existen, campos nulls, etc.)
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={"detail": "Error de integridad de datos en la solicitud."}
        )

    # Puedes agregar más manejadores aquí si lo necesitas en el futuro, por ejemplo:
    # @app.exception_handler(Exception)
    # async def global_exception_handler(...):