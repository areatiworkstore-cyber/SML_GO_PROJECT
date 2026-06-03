from datetime import date, time
from typing import Optional
from pydantic import BaseModel
from app.schemas.client import ClientSimpleResponse

class ClientScheduleBase(BaseModel):
    day: date
    start_time: time
    observation: Optional[str]
    active: bool = True

class ClientScheduleCreate(ClientScheduleBase):
    client_id: int
    user_id: int

class ClientScheduleUpdate(ClientScheduleBase):
    client_id: Optional[int] = None
    user_id: Optional[int] = None
    day: Optional[date] = None
    start_time: Optional[time] = None
    observation: Optional[str] = None
    active: Optional[bool] = None

class ClientScheduleResponse(ClientScheduleBase):
    id: int
    client_id: int
    user_id: int
    client: ClientSimpleResponse

    class Config:
        from_attributes = True