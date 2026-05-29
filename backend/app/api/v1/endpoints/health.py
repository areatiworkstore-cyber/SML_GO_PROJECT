from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy.sql import text
from app.core.database import get_db
import time

router = APIRouter()

@router.get("/db-check", tags=["health"])
def test_db_connection(db: Session = Depends(get_db)):
    """
    Verifica que la conexión con la base de datos MySQL esté activa y sea correcta.
    """
    start_time = time.time()
    try:
        # Execute simple query to test connection
        db.execute(text("SELECT 1"))
        latency = round((time.time() - start_time) * 1000, 2)
        return {
            "status": "online",
            "database": "connected",
            "latency_ms": latency,
            "message": "Conexión a la base de datos establecida correctamente."
        }
    except Exception as e:
        latency = round((time.time() - start_time) * 1000, 2)
        raise HTTPException(
            status_code=500,
            detail={
                "status": "offline",
                "database": "disconnected",
                "latency_ms": latency,
                "error": str(e),
                "message": "No se pudo establecer conexión con la base de datos. Verifica tu archivo .env y el servidor MySQL."
            }
        )
