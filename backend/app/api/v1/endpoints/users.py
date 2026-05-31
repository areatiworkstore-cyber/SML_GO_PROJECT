from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user, check_roles
from app.models.user import User
from app.schemas.user import UserCreate, UserResponse, UserPerfilResponse, RoleUserResponse, RoleUserUpdate, UserUpdate
from app.crud import crud_user

router = APIRouter()

@router.post("/", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def create_user(
    *,
    db: Session = Depends(get_db),
    user_in: UserCreate,
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Create a new user/seller (Only accessible by Admin).
    """
    user_by_email = crud_user.get_user_by_email(db, email=user_in.email)
    if user_by_email:
        raise HTTPException(
            status_code=400,
            detail="The user with this email already exists in the system."
        )
    user_by_code = crud_user.get_user_by_code(db, code=user_in.code)
    if user_by_code:
        raise HTTPException(
            status_code=400,
            detail="The user with this code already exists in the system."
        )
    return crud_user.create_user(db, user_in=user_in)


@router.get("/", response_model=List[UserResponse])
def read_users(
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Recupera todos los usuarios (Vendedores, Administradores, etc. - Solo Admin).
    """
    return crud_user.get_users(db, skip=skip, limit=limit)

@router.put("/{user_id}", response_model=UserResponse, status_code=status.HTTP_200_OK)
def update_user(
    user_id: int,
    user_in: UserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Actualiza los datos de un usuario (Solo Admin).
    """
    db_user = crud_user.get_user_by_id(db, user_id=user_id)
    if not db_user:
        raise HTTPException(
            status_code=404,
            detail="Usuario no encontrado"
        )
    return crud_user.update_user(db, db_user=db_user, user_in=user_in.model_dump(exclude_unset=True))

@router.get("/me", response_model=UserPerfilResponse)
def read_user_me(
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene el perfil del usuario actual.
    """
    return current_user

@router.get("/role_users", response_model=List[RoleUserResponse], status_code=status.HTTP_200_OK)
def read_role_users(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Recupera todos los usuarios asignados a un rol (Solo Admin).
    """
    role_users = crud_user.get_role_users(db, skip=skip, limit=limit)
    return role_users

@router.get("/role/{role_user_id}", response_model=RoleUserResponse, status_code=status.HTTP_200_OK)
def read_role_user(
    role_user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Obtiene un usuario específico asignado a un rol por su ID (Solo Admin).
    """
    role_user = crud_user.get_role_user_by_id(db, role_user_id=role_user_id)
    if not role_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="El usuario asignado al rol no existe."
        )
    return role_user


@router.put("/role/{role_user_id}", response_model=RoleUserResponse, status_code=status.HTTP_200_OK)
def update_role_user(
    role_user_id: int,
    role_user_in: RoleUserUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Actualiza un usuario específico asignado a un rol por su ID (Solo Admin).
    """
    role_user = crud_user.get_role_user_by_id(db, role_user_id=role_user_id)
    if not role_user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="El usuario asignado al rol no existe."
        )
        
    # Actualizamos dinámicamente los campos enviados (ej. cambiar el role_id de un usuario)
    for field, value in role_user_in.model_dump(exclude_unset=True).items():
        setattr(role_user, field, value)
        
    db.commit()
    db.refresh(role_user)
    return role_user