from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship
from app.core.database import Base

class DocumentType(Base):
    __tablename__ = "document_type"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    description = Column(String(10), nullable=False)

    users = relationship("User", back_populates="document_type")
    clients = relationship("Client", back_populates="document_type")


class BusinessType(Base):
    __tablename__ = "business_type"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    description = Column(String(50), nullable=False)

    clients = relationship("Client", back_populates="business_type")


class ClientGroup(Base):
    __tablename__ = "client_group"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    description = Column(String(3), nullable=False)

    clients = relationship("Client", back_populates="client_group")

class Role(Base):
    __tablename__ = "role"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    role = Column(String(20), nullable=False)

    users = relationship("RoleUser", back_populates="role_details")

