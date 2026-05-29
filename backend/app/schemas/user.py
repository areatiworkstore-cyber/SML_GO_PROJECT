from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr

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
    cellphone: Optional[str] = None
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    active: Optional[bool] = None

class RoleUserResponse(BaseModel):
    role_id: int
    role_name: Optional[str] = None

    class Config:
        from_attributes = True

class UserResponse(UserBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
