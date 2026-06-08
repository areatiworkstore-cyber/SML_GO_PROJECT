from typing import List, Optional
from fastapi import APIRouter, Depends, status, Query, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.deps import get_current_user
from app.api.deps import check_roles
from app.models.user import User

from app.schemas.master_data import BusinessTypeResponse, DocumentTypeResponse, ClientGroupResponse, RoleResponse, RoleUpdate
from app.crud import crud_master_data

router = APIRouter()

@router.get("/business-types", response_model=List[BusinessTypeResponse], status_code=status.HTTP_200_OK)
def get_business_types(
    business_type_id: Optional[int] = Query(default=None, alias="id"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista todos los tipos de negocio activos.
    """
    if business_type_id:
        business_type = crud_master_data.get_business_type_by_id(db, business_type_id=business_type_id)
        if not business_type:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="El tipo de negocio no existe")
        return business_type
    return crud_master_data.get_business_types(db)


@router.get("/document-types", response_model=List[DocumentTypeResponse], status_code=status.HTTP_200_OK)
def get_document_types(
    document_type_id: Optional[int] = Query(default=None, alias="id"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista todos los tipos de documento activos.
    """
    if document_type_id:
        document_type = crud_master_data.get_document_type_by_id(db, document_type_id=document_type_id)
        if not document_type:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="El tipo de documento no existe")
        return document_type
    return crud_master_data.get_document_types(db)


@router.get("/client-groups", response_model=List[ClientGroupResponse], status_code=status.HTTP_200_OK)
def get_client_groups(
    client_group_id: Optional[int] = Query(default=None, alias="id"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Lista todos los grupos de clientes activos.
    """
    if client_group_id:
        client_group = crud_master_data.get_client_group_by_id(db, client_group_id=client_group_id)
        if not client_group:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="El grupo de cliente no existe")
        return client_group
    return crud_master_data.get_client_groups(db)

@router.get("/roles", response_model=List[RoleResponse], status_code=status.HTTP_200_OK)
def read_roles(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Endpoint para listar todos los roles disponibles.
    """
    return crud_master_data.get_roles(db)

@router.get("/role/{role_id}", response_model=RoleResponse, status_code=status.HTTP_200_OK)
def read_role(
    role_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Endpoint para obtener un rol por ID.
    """
    role = crud_master_data.get_role_by_id(db, role_id=role_id)
    if not role:
        raise HTTPException(status_code=404, detail="Role no encontrado")
    return role