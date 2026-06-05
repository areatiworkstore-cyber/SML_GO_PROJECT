from datetime import datetime, date
from typing import Optional, List
from pydantic import BaseModel

# Waypoint Schemas
class WaypointBase(BaseModel):
    address: str
    latitud: Optional[float] = None
    longitud: Optional[float] = None
    order_sequence: int
    client_id: int
    status: Optional[str] = "PENDIENTE"  # PENDIENTE, VISITA, CANCELADA
    comment: Optional[str] = None

class WaypointCreate(WaypointBase):
    pass

class WaypointUpdate(BaseModel):
    status: Optional[str] = None  # VISITA, CANCELADA
    visited_at: Optional[datetime] = None
    comment: Optional[str] = None
    order_sequence: Optional[int] = None
    address: Optional[str] = None
    latitud: Optional[float] = None
    longitud: Optional[float] = None

class WaypointResponse(WaypointBase):
    id: int
    route_id: int
    visited_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# Route Schemas
class RouteBase(BaseModel):
    name: str
    scheduled_date: date
    user_id: int
    active: Optional[bool] = True

class RouteCreate(RouteBase):
    pass

class RouteUpdate(BaseModel):
    name: Optional[str] = None
    scheduled_date: Optional[date] = None
    user_id: Optional[int] = None
    active: Optional[bool] = None

class RouteResponse(RouteBase):
    id: int
    created_at: datetime
    updated_at: datetime
    waypoints: List[WaypointResponse] = []

    class Config:
        from_attributes = True
