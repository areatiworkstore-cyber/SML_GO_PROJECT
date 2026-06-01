from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.client_schedule import ClientScheduleResponse, ClientScheduleCreate, ClientScheduleUpdate
from app.crud import crud_client_schedule
from datetime import date, time

router = APIRouter()

@router.get("/{id}", response_model=ClientScheduleResponse)
async def read_client_schedule(
    id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Obtener una programación de cliente por ID"""
    client_schedule = crud_client_schedule.get_client_schedule_by_id(db, id)
    if not client_schedule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Programación de cliente no encontrada",
        )
    # 🔒 Control de accesos OWASP: Vendedores solo ven sus programaciones
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client_schedule.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para acceder a esta programación",
        )
    return client_schedule

@router.get("/", response_model=List[ClientScheduleResponse])
async def read_client_schedules(
    skip: int = 0,
    limit: int = 10,
    user_id: Optional[int] = None,
    client_id: Optional[int] = None,
    day: Optional[date] = None,
    start_time: Optional[time] = None,
    active: Optional[bool] = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Obtener programaciones de clientes con soporte multi-filtro dinámico."""
    
    # 🔒 Control de accesos OWASP:
    # Si el usuario no es ADMIN, forzamos el filtro de user_id a su propio ID.
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles:
        user_id = current_user.id
    elif user_id is not None and current_user.id != user_id:
        # Si es ADMIN pero provee un user_id y queremos validar algo adicional, o permitir acceso total (ADMIN puede ver cualquiera)
        pass
    
    # Invocamos la consulta enviando toda la carga útil de los filtros
    client_schedules = crud_client_schedule.get_client_schedules(
        db=db,
        skip=skip,
        limit=limit,
        user_id=user_id,
        client_id=client_id,
        day=day,
        start_time=start_time,
        active=active
    )
    
    return client_schedules

@router.post("/", response_model=ClientScheduleResponse)
async def create_client_schedule(
    client_schedule: ClientScheduleCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Crear una nueva programación de cliente"""
    # 🔒 Control de accesos OWASP: Forzar que el user_id de la agenda sea el del usuario actual si no es ADMIN
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles:
        client_schedule.user_id = current_user.id
    elif client_schedule.user_id != current_user.id and "ADMIN" not in roles:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para crear programaciones para otro usuario",
        )
    
    db_client_schedule = crud_client_schedule.create_client_schedule(db, client_schedule)
    return db_client_schedule

@router.put("/{id}", response_model=ClientScheduleResponse)
async def update_client_schedule(
    id: int,
    client_schedule: ClientScheduleUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Actualizar una programación de cliente"""
    # Verificar que el usuario de la programación sea el usuario actual
    if client_schedule.user_id is not None and client_schedule.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para actualizar programaciones de otro usuario",
        )
    
    db_client_schedule = crud_client_schedule.update_client_schedule(db, id, client_schedule)
    if not db_client_schedule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Programación de cliente no encontrada",
        )
    return db_client_schedule

@router.delete("/{id}")
async def delete_client_schedule(
    id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Eliminar una programación de cliente"""
    # Verificar que el usuario tenga permiso para eliminar la programación
    client_schedule = crud_client_schedule.get_client_schedule_by_id(db, id)
    if not client_schedule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Programación de cliente no encontrada",
        )
    
    if client_schedule.user_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="No tienes permiso para eliminar esta programación",
        )
    
    db_client_schedule = crud_client_schedule.delete_client_schedule(db, id)
    if not db_client_schedule:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Programación de cliente no encontrada",
        )
    return {"ok": True, "message": "Programación de cliente eliminada exitosamente"}