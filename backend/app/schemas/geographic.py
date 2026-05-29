from typing import Optional
from pydantic import BaseModel

# --- SCHEMAS DE DEPARTAMENTO ---

class DepartmentResponse(BaseModel):
    id: int
    name: str
    active: bool

    class Config:
        from_attributes = True


# --- SCHEMAS DE PROVINCIA ---

class ProvinceResponse(BaseModel):
    id: int
    name: str
    active: bool
    department_id: int  # Relación con su departamento correspondiente

    class Config:
        from_attributes = True


# --- SCHEMAS DE DISTRITO ---

class DistrictResponse(BaseModel):
    id: int
    name: str
    active: bool
    province_id: int  # Relación con su provincia correspondiente

    class Config:
        from_attributes = True