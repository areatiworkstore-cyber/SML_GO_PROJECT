from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user, check_roles
from app.models.user import User
from app.schemas.user import UserCreate, UserResponse, UserPerfilResponse, UserUpdate
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

@router.get("/active", response_model=List[UserResponse])
def read_users_active(
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 30,
    current_user: User = Depends(check_roles(["ADMIN"]))
):
    """
    Recupera todos los usuarios activos (Vendedores, Administradores, etc. - Solo Admin y Agente).
    """
    return crud_user.get_users_active(db, skip=skip, limit=limit)

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

@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_user(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"])) 
):
    """
    Elimina lógicamente un usuario (desactivándolo).
    """
    db_user = crud_user.get_user_by_id(db, user_id=user_id)
    if not db_user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    # Prevenir borrado del propio usuario
    if db_user.id == current_user.id:
        raise HTTPException(
            status_code=400, 
            detail="No puedes eliminar tu propia cuenta desde aquí."
        )
        
    success = crud_user.delete_user(db, db_user=db_user)
    
    if not success:
         raise HTTPException(status_code=500, detail="No se pudo eliminar el usuario")

@router.patch("/{user_id}/restore", status_code=status.HTTP_200_OK)
def restore_user(
    user_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(check_roles(["ADMIN"])) 
):
    """
    Restaura un usuario eliminado lógicamente.
    """
    db_user = crud_user.get_user_by_id(db, user_id=user_id)
    if not db_user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    # Restaurar el usuario
    success = crud_user.restore_user(db, db_user=db_user)
    
    if not success:
         raise HTTPException(status_code=500, detail="No se pudo restaurar el usuario")
    
    return db_user

@router.get("/me", response_model=UserPerfilResponse)
def read_user_me(
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene el perfil del usuario actual.
    """
    return {
        "id": current_user.id,
        "code": current_user.code,
        "first_name": current_user.first_name,
        "second_name": current_user.second_name,
        "first_surname": current_user.first_surname,
        "second_surname": current_user.second_surname,
        "document_type": current_user.document_type,
        "document_number": current_user.document_number,
        "cellphone": current_user.cellphone,
        "email": current_user.email,
        "active": current_user.active,
        "roles": [
            ru.role_details.role
            for ru in current_user.roles
            if ru.role_details
        ]
    }

