from sqlalchemy import Column, Integer, String, Boolean, ForeignKey
from sqlalchemy.orm import relationship
from app.core.database import Base

class Department(Base):
    __tablename__ = "department"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(50), nullable=False)
    active = Column(Boolean, default=True)

    provinces = relationship("Province", back_populates="department")


class Province(Base):
    __tablename__ = "province"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(50), nullable=False)
    active = Column(Boolean, default=True)
    department_id = Column(Integer, ForeignKey("department.id"), nullable=False)

    department = relationship("Department", back_populates="provinces")
    districts = relationship("District", back_populates="province")


class District(Base):
    __tablename__ = "district"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    name = Column(String(50), nullable=False)
    active = Column(Boolean, default=True, nullable=False)
    province_id = Column(Integer, ForeignKey("province.id"), nullable=False)

    province = relationship("Province", back_populates="districts")
    clients = relationship("Client", back_populates="district")
