from typing import Optional
from pydantic import BaseModel

# --- SCHEMA DE TIPO DE NEGOCIO ---
class BusinessTypeResponse(BaseModel):
    id: int
    description: str

    class Config:
        from_attributes = True

# --- SCHEMA DE TIPO DE DOCUMENTO ---
class DocumentTypeResponse(BaseModel):
    id: int
    description: str

    class Config:
        from_attributes = True

# --- SCHEMA DE CLIENTE ---
class ClientGroupResponse(BaseModel):
    id: int
    description: str

    class Config:
        from_attributes = True