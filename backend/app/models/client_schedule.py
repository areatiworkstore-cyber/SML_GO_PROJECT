from sqlalchemy import Column, Integer, Time, Boolean, Date, ForeignKey, String
from sqlalchemy.orm import relationship
from app.core.database import Base

class ClientSchedule(Base):
    __tablename__ = "client_schedule"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)

    client_id = Column(Integer, ForeignKey("client.id"), nullable=False)
    user_id = Column(Integer, ForeignKey("user.id"), nullable=False)
    day = Column(Date, nullable=False)
    start_time = Column(Time, nullable=False)
    observation = Column(String, nullable=True)
    active = Column(Boolean, default=True)

    client = relationship("Client", back_populates="client_schedules")
    user = relationship("User", back_populates="client_schedules")