from datetime import datetime
from typing import Optional
from pydantic import BaseModel

# --- SCHEMA DE CLIENTE ---
class ClientBase(BaseModel):
    code: Optional[str] = None
    name: Optional[str] = None
    document_type_id: Optional[int] = None
    document_number: Optional[str] = None
    address: Optional[str] = None
    district_id: Optional[int] = None
    business_type_id: Optional[int] = None
    client_group_id: Optional[int] = None
    cellphone: Optional[str] = None
    telephone: Optional[str] = None
    active: Optional[bool] = True
    latitud: Optional[float] = None
    longitud: Optional[float] = None
    observation: Optional[str] = None

class ClientCreate(ClientBase):
    user_id: int  # The seller this client belongs to
    supplier_id: Optional[int] = None

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
    supplier_id: Optional[int] = None

class ClientNextCodeResponse(BaseModel):
    next_code: str

class ClientResponse(ClientBase):
    id: int
    user_id: int
    supplier_id: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True