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

'''
Metodos CRUD para el modelo RoleUser
'''
def get_role_user_by_id(db: Session, role_user_id: int):
    return db.query(RoleUser).filter(RoleUser.id == role_user_id).first()

def get_role_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(RoleUser).offset(skip).limit(limit).all()