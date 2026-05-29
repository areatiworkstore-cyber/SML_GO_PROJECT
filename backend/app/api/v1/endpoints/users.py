from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user, check_roles
from app.models.user import User
from app.schemas.user import UserCreate, UserResponse, UserPerfilResponse
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
    Retrieve all users (Sellers, Admins, etc. - Admin only).
    """
    return crud_user.get_users(db, skip=skip, limit=limit)


@router.get("/me", response_model=UserPerfilResponse)
def read_user_me(
    current_user: User = Depends(get_current_user)
):
    """
    Get current logged-in user profile.
    """
    return current_user
