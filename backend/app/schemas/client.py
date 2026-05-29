from datetime import datetime
from typing import Optional
from pydantic import BaseModel

class ClientBase(BaseModel):
    code: str
    name: str
    document_type_id: int
    document_number: str
    address: str
    district_id: int
    business_type_id: int
    client_group_id: int
    cellphone: Optional[str] = None
    telephone: Optional[str] = None
    active: Optional[bool] = True
    latitud: Optional[float] = None
    longitud: Optional[float] = None
    observation: Optional[str] = None

class ClientCreate(ClientBase):
    user_id: int  # The seller this client belongs to

class ClientUpdate(BaseModel):
    name: Optional[str] = None
    address: Optional[str] = None
    district_id: Optional[int] = None
    business_type_id: Optional[int] = None
    client_group_id: Optional[int] = None
    cellphone: Optional[str] = None
    telephone: Optional[str] = None
    active: Optional[bool] = None
    latitud: Optional[float] = None
    longitud: Optional[float] = None
    user_id: Optional[int] = None
    observation: Optional[str] = None

class ClientResponse(ClientBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
