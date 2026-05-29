from sqlalchemy.orm import Session
from datetime import date, time
from typing import Optional
from app.models.client_schedule import ClientSchedule as ClientScheduleModel
from app.schemas.client_schedule import ClientScheduleCreate, ClientScheduleUpdate

def get_client_schedule_by_id(db: Session, id: int):
    return db.query(ClientScheduleModel).filter(ClientScheduleModel.id == id).first()

def get_client_schedules(
    db: Session, 
    skip: int = 0, 
    limit: int = 10,
    user_id: Optional[int] = None,
    client_id: Optional[int] = None,
    day: Optional[date] = None,
    start_time: Optional[time] = None,
    active: Optional[bool] = None
):
    query = db.query(ClientScheduleModel)
    
    # Se van encadenando los filtros automáticamente si el parámetro fue enviado
    if user_id is not None:
        query = query.filter(ClientScheduleModel.user_id == user_id)
    if client_id is not None:
        query = query.filter(ClientScheduleModel.client_id == client_id)
    if day is not None:
        query = query.filter(ClientScheduleModel.day == day)
    if start_time is not None:
        query = query.filter(ClientScheduleModel.start_time == start_time)
    if active is not None:
        query = query.filter(ClientScheduleModel.active == active)
        
    # Finalmente aplicamos la paginación y retornamos los resultados ordenados por hora
    return query.order_by(ClientScheduleModel.start_time.asc()).offset(skip).limit(limit).all()

def create_client_schedule(db: Session, client_schedule: ClientScheduleCreate):
    db_client_schedule = ClientScheduleModel(
        client_id=client_schedule.client_id,
        user_id=client_schedule.user_id,
        day=client_schedule.day,
        start_time=client_schedule.start_time,
        observation=client_schedule.observation,
        active=client_schedule.active,
    )
    db.add(db_client_schedule)
    db.commit()
    db.refresh(db_client_schedule)
    return db_client_schedule

def update_client_schedule(db: Session, id: int, client_schedule: ClientScheduleUpdate):
    db_client_schedule = get_client_schedule_by_id(db, id)
    if not db_client_schedule:
        return None
    
    # .dict(exclude_unset=True) o .model_dump(exclude_unset=True) si usas Pydantic v2
    update_data = client_schedule.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_client_schedule, field, value)
    
    db.commit()
    db.refresh(db_client_schedule)
    return db_client_schedule

def delete_client_schedule(db: Session, id: int):
    db_client_schedule = get_client_schedule_by_id(db, id)
    if not db_client_schedule:
        return None
    db.delete(db_client_schedule)
    db.commit()
    return db_client_schedule