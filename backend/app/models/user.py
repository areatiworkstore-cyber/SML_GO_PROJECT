from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class User(Base):
    __tablename__ = "user"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
    
    code = Column(String(6), unique=True, nullable=False, index=True)
    first_name = Column(String(50), nullable=False)
    second_name = Column(String(50), nullable=False)
    first_surname = Column(String(50), nullable=False)
    second_surname = Column(String(50), nullable=False)
    document_type_id = Column(Integer, ForeignKey("document_type.id"), nullable=False)
    document_number = Column(String(11), unique=True, nullable=False, index=True)
    cellphone = Column(String(9), nullable=False)
    email = Column(String(30), nullable=False, index=True)
    password = Column(String(255), nullable=False)

    document_type = relationship("DocumentType", back_populates="users")
    roles = relationship("RoleUser", back_populates="user")
    clients = relationship("Client", back_populates="user")
    routes = relationship("Route", back_populates="user")
    client_schedules = relationship("ClientSchedule", back_populates="user")    

class RoleUser(Base):
    __tablename__ = "role_user"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False)
    role_id = Column(Integer, ForeignKey("role.id"), nullable=False)

    user = relationship("User", back_populates="roles")
    role_details = relationship("Role", back_populates="users")
