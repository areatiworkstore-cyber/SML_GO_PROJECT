from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Response
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.config import settings
from app.core.security import verify_password, create_access_token
from app.crud.crud_user import get_user_by_email, get_user_by_code
from app.schemas.auth import Token

router = APIRouter()

@router.post("/login")
def login_for_access_token(
    response: Response,
    db: Session = Depends(get_db),
    form_data: OAuth2PasswordRequestForm = Depends()
):
    # Intento obtener el usuario por email, si no existe, intento obtenerlo por código
    user = get_user_by_email(db, email=form_data.username)
    if not user:
        user = get_user_by_code(db, code=form_data.username)
        
    # Si no existe el usuario o la contraseña es incorrecta, lanzo una excepción
    if not user or not verify_password(form_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo o contraseña incorrectos",
        )
        
    roles = [ru.role_details.role for ru in user.roles]
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        subject=user.id, expires_delta=access_token_expires
    )

    response.set_cookie(
        key="access_token", # Nombre de la cookie
        value=access_token, # El token JWT generado
        httponly=True, # La cookie no es accesible por JavaScript
        secure=True, # En producción cross-site (sml.com.pe <-> workstore.com.pe) debe ser True
        samesite="none", # Permite el envío de cookies cross-site entre dominios distintos
        max_age=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60 # Tiempo de expiración de la cookie
    )
    
    return {
        "message": "Login exitoso",
        "roles": roles
    }

@router.post("/logout")

def logout(response: Response):
    response.delete_cookie(
        key="access_token", # Debe ser el mismo nombre que en el login
        httponly=True,
        secure=True,
        samesite="none"
    )
    return {
        "success": True
    }


