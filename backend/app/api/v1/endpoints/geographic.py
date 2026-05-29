from typing import List, Optional
from fastapi import APIRouter, Depends, status, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User

from app.schemas.geographic import DepartmentResponse, ProvinceResponse, DistrictResponse
from app.crud import crud_geographic

router = APIRouter()

@router.get("/departments", response_model=List[DepartmentResponse], status_code=status.HTTP_200_OK)
def get_departments(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista todos los departamentos activos.
    """
    return crud_geographic.get_departments(db)


@router.get("/provinces", response_model=List[ProvinceResponse], status_code=status.HTTP_200_OK)
def get_provinces(
    department_id: Optional[int] = Query(None, description="Filtrar provincias por ID de departamento"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista las provincias. Si se provee 'department_id', se filtran las provincias de ese departamento.
    """
    if department_id is not None:
        return crud_geographic.get_provinces_by_department(db, department_id=department_id)
    return crud_geographic.get_provinces(db)


@router.get("/districts", response_model=List[DistrictResponse], status_code=status.HTTP_200_OK)
def get_districts(
    province_id: Optional[int] = Query(None, description="Filtrar distritos por ID de provincia"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista los distritos. Si se provee 'province_id', se filtran los distritos de esa provincia.
    """
    if province_id is not None:
        return crud_geographic.get_districts_by_province(db, province_id=province_id)
    return crud_geographic.get_districts(db)