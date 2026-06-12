from sqlalchemy import Column, Integer, String, Boolean, DateTime, Date, ForeignKey, Double, Text, func
from sqlalchemy.orm import relationship
from app.core.database import Base

class Route(Base):
    __tablename__ = "route"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
    
    name = Column(String(50), nullable=False)
    scheduled_date = Column(Date, nullable=False)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False)
    active = Column(Boolean, default=True)

    user = relationship("User", back_populates="routes")
    waypoints = relationship("Waypoint", back_populates="route", cascade="all, delete-orphan")


class Waypoint(Base):
    __tablename__ = "waypoint"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    route_id = Column(Integer, ForeignKey("route.id"), nullable=False)
    address = Column(Text, nullable=False)
    latitud = Column(Double)
    longitud = Column(Double)
    order_sequence = Column(Integer, nullable=False)
    client_id = Column(Integer, ForeignKey("client.id"), nullable=False)
    status = Column(String(10), default="PENDIENTE", nullable=False)  # PENDIENTE, VISITA, CANCELADA
    visited_at = Column(DateTime, nullable=True)
    url_photo = Column(Text, nullable=True)
    comment = Column(Text, nullable=True)

    route = relationship("Route", back_populates="waypoints")
    client = relationship("Client", back_populates="waypoints")
