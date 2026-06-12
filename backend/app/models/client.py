from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, Double, Text, func, UniqueConstraint
from sqlalchemy.orm import relationship
from app.core.database import Base

class Client(Base):
    __tablename__ = "client"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
    
    code = Column(String(6), nullable=True, index=True)
    name = Column(String(50), nullable=True)
    document_type_id = Column(Integer, ForeignKey("document_type.id"), nullable=True)
    document_number = Column(String(11), unique=True, nullable=True, index=True)
    address = Column(Text, nullable=True)
    district_id = Column(Integer, ForeignKey("district.id"), nullable=True)
    business_type_id = Column(Integer, ForeignKey("business_type.id"), nullable=True)
    client_group_id = Column(Integer, ForeignKey("client_group.id"), nullable=True)
    cellphone = Column(String(9))
    telephone = Column(String(9))
    active = Column(Boolean, default=True)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False)
    
    latitud = Column(Double)
    longitud = Column(Double)
    observation = Column(Text)

    # Relationships
    document_type = relationship("DocumentType", back_populates="clients")
    district = relationship("District", back_populates="clients")
    business_type = relationship("BusinessType", back_populates="clients")
    client_group = relationship("ClientGroup", back_populates="clients")
    user = relationship("User", back_populates="clients")
    waypoints = relationship("Waypoint", back_populates="client")
    client_schedules = relationship("ClientSchedule", back_populates="client")

    __table_args__ = (
        UniqueConstraint('code', name='uq_client_code'),
    )

    # ---- PROPIEDADES CALCULADAS AL FINAL DE LA CLASE ----
    @property
    def province(self):
        if self.district and hasattr(self.district, 'province') and self.district.province:
            return self.district.province
        return None

    @property
    def department(self):
        if (self.district and 
                hasattr(self.district, 'province') and self.district.province and 
                hasattr(self.district.province, 'department') and self.district.province.department):
            return self.district.province.department
        return None