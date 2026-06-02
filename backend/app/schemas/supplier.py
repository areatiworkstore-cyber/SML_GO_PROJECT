from datetime import datetime
from typing import Optional
from pydantic import BaseModel


class SupplierBase(BaseModel):
    code: str
    names: str
    active: Optional[bool] = True


class SupplierCreate(SupplierBase):
    pass


class SupplierUpdate(BaseModel):
    code: Optional[str] = None
    names: Optional[str] = None
    active: Optional[bool] = None


class SupplierResponse(SupplierBase):
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
