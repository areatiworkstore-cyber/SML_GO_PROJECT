from datetime import datetime
from typing import Optional
from pydantic import BaseModel
from app.schemas.geographic import DistrictResponse, ProvinceResponse, DepartmentResponse

# --- SCHEMA DE CLIENTE ---
class ClientBase(BaseModel):
    code: Optional[str] = None
    name: Optional[str] = None
    document_type_id: Optional[int] = None
    document_number: Optional[str] = None
    address: Optional[str] = None
    district_id: Optional[int] = None
    district: Optional[DistrictResponse] = None
    province: Optional[ProvinceResponse] = None
    department: Optional[DepartmentResponse] = None
    business_type_id: Optional[int] = None
    client_group_id: Optional[int] = None
    cellphone: Optional[str] = None
    telephone: Optional[str] = None
    active: Optional[bool] = True
    latitud: Optional[float] = None
    longitud: Optional[float] = None
    observation: Optional[str] = None

class ClientCreate(ClientBase):
    user_id: Optional[int] = None  # The seller this client belongs to

class ClientUpdate(ClientBase):
    user_id: Optional[int] = None

class ClientNextCodeResponse(BaseModel):
    next_code: str

class ClientResponse(ClientBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

# --- SCHEMA SIMPLE PARA INYECTAR EN OTROS MODELOS (como ClientSchedule) ---
class ClientSimpleResponse(BaseModel):
    id: int
    code: Optional[str]
    name: Optional[str]
    address: Optional[str]
    cellphone: Optional[str]
    latitud: Optional[float]
    longitud: Optional[float]
    
    class Config:
        from_attributes = True