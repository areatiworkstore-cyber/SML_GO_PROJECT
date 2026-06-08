from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr
from app.schemas.master_data import DocumentTypeResponse

# --- SCHEMA DE ROL USUARIO ---
class RoleUserBase(BaseModel):
    user_id: int
    role_id: int

class RoleUserUpdate(RoleUserBase):
    pass

class RoleUserResponse(RoleUserBase):
    id: int

    class Config:
        from_attributes = True

# --- SCHEMA DE USER ---
class UserBase(BaseModel):
    code: str
    first_name: str
    second_name: str
    first_surname: str
    second_surname: str
    document_type_id: int
    document_number: str
    cellphone: str
    email: EmailStr

class UserPerfilResponse(BaseModel):
    id: int
    code: str
    first_name: str
    second_name: str
    first_surname: str
    second_surname: str
    document_type   : DocumentTypeResponse | None = None
    document_number : str
    cellphone       : str
    email           : str
    roles           : List[RoleUserResponse] = []

    class Config:
        from_attributes = True

class UserCreate(UserBase):
    password: str
    role_ids: List[int] = []

class UserUpdate(BaseModel):
    first_name: Optional[str] = None
    second_name: Optional[str] = None
    first_surname: Optional[str] = None
    second_surname: Optional[str] = None
    document_type_id: Optional[int] = None
    document_number: Optional[str] = None
    cellphone: Optional[str] = None
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    active: Optional[bool] = None
    role_ids: Optional[List[int]] = None

class UserResponse(UserBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
