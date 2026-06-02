from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.supplier import SupplierCreate, SupplierResponse
from app.crud import crud_supplier


router = APIRouter()

@router.get("/", response_model=List[SupplierResponse])
async def read_suppliers(
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Listar proveedores activos/todos"""
    return crud_supplier.get_multi(db, skip=skip, limit=limit)

@router.get("/search", response_model=List[SupplierResponse])
async def search_suppliers_by_code(
    code: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Buscar proveedores por coincidencia de código (Lupa Frontend)"""
    if not code:
        return []
    return crud_supplier.search_by_code(db, text=code)

@router.post("/", response_model=SupplierResponse, status_code=status.HTTP_201_CREATED)
async def create_supplier(
    supplier_in: SupplierCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """Crear un nuevo proveedor manualmente"""
    existing = crud_supplier.get_by_code(db, code=supplier_in.code)
    if existing:
        raise HTTPException(status_code=400, detail="El código de proveedor ya se encuentra registrado.")
    return crud_supplier.create(db, obj_in=supplier_in)