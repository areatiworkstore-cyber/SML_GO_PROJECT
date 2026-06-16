from sqlalchemy.orm import Session
from app.models.user import User, RoleUser
from app.schemas.user import UserCreate
from app.core.security import get_password_hash

'''
Metodos CRUD para el modelo User
'''
def get_user_by_id(db: Session, user_id: int):
    return db.query(User).filter(User.id == user_id).first()

def get_user_by_code(db: Session, code: str):
    return db.query(User).filter(User.code == code).first()

def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(User).offset(skip).limit(limit).all()

def create_user(db: Session, user_in: UserCreate) -> User:
    hashed_password = get_password_hash(user_in.password)
    db_user = User(
        code=user_in.code,
        first_name=user_in.first_name,
        second_name=user_in.second_name,
        first_surname=user_in.first_surname,
        second_surname=user_in.second_surname,
        document_type_id=user_in.document_type_id,
        document_number=user_in.document_number,
        cellphone=user_in.cellphone,
        email=user_in.email,
        password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    
    # Assign roles
    if user_in.role_ids:
        for role_id in user_in.role_ids:
            role_user = RoleUser(user_id=db_user.id, role_id=role_id)
            db.add(role_user)
        db.commit()
        db.refresh(db_user)
        
    return db_user

def update_user(db: Session, db_user: User, user_in: dict) -> User:
    role_ids = user_in.pop("role_ids", None)
    
    for field, value in user_in.items():
        if field == "password":
            if value: # Solo actualiza si hay un nuevo password
                setattr(db_user, field, get_password_hash(value))
        elif value is not None:
            setattr(db_user, field, value)
            
    if role_ids is not None:
        db.query(RoleUser).filter(RoleUser.user_id == db_user.id).delete()
        for r_id in role_ids:
            db.add(RoleUser(user_id=db_user.id, role_id=r_id))
            
    db.commit()
    db.refresh(db_user)
    return db_user

def delete_user(db: Session, db_user: User) -> bool:
    """
    Elimina lógicamente el usuario (soft-delete).
    """
    # Marcar como inactivo
    db_user.active = False
    
    # Marcar como eliminado (opcional, si tienes campo deleted_at)
    # from datetime import datetime
    # db_user.deleted_at = datetime.utcnow()
    
    db.commit()
    db.refresh(db_user)
    return True

def restore_user(db: Session, db_user: User) -> bool:
    """
    Restaura un usuario eliminado lógicamente.
    """
    db_user.active = True
    db.commit()
    db.refresh(db_user)
    return True

'''
Metodos CRUD para el modelo RoleUser
'''
def get_role_user_by_id(db: Session, role_user_id: int):
    return db.query(RoleUser).filter(RoleUser.id == role_user_id).first()

def get_role_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(RoleUser).offset(skip).limit(limit).all()