from datetime import datetime
from sqlalchemy.orm import Session
from app.models.route import Route, Waypoint
from app.schemas.route import RouteCreate, RouteUpdate, WaypointUpdate, WaypointCreate

def get_route_by_id(db: Session, route_id: int):
    return db.query(Route).filter(Route.id == route_id).first()

def get_routes(db: Session, skip: int = 0, limit: int = 100, user_id: int = None):
    query = db.query(Route)
    if user_id is not None:
        query = query.filter(Route.user_id == user_id)
    return query.offset(skip).limit(limit).all()

def create_route(db: Session, route_in: RouteCreate) -> Route:
    db_route = Route(
        name=route_in.name,
        scheduled_date=route_in.scheduled_date,
        user_id=route_in.user_id,
        active=route_in.active
    )
    db.add(db_route)
    db.commit()
    db.refresh(db_route)

    return db_route

def update_route(db: Session, db_route: Route, route_in: RouteUpdate) -> Route:
    update_data = route_in.model_dump(exclude_unset=True)
    for field in update_data:
        setattr(db_route, field, update_data[field])
    db.add(db_route)
    db.commit()
    db.refresh(db_route)
    return db_route

def delete_route(db: Session, db_route: Route) -> None:
    """Elimina una ruta y todas sus paradas."""
    #Eliminar paradas primero
    for wp in db_route.waypoints:
        db.delete(wp)
    #Eliminar ruta
    db.delete(db_route)
    db.commit()
    return True

def get_waypoint_by_id(db: Session, waypoint_id: int):
    return db.query(Waypoint).filter(Waypoint.id == waypoint_id).first()

def update_waypoint_status(db: Session, db_waypoint: Waypoint, wp_in: WaypointUpdate) -> Waypoint:
    update_data = wp_in.model_dump(exclude_unset=True)
    for field in update_data:
        setattr(db_waypoint, field, update_data[field])
    
    if wp_in.status in ["VISITA", "CANCELADA"] and not db_waypoint.visited_at:
        db_waypoint.visited_at = datetime.now()
        
    db.add(db_waypoint)
    db.commit()
    db.refresh(db_waypoint)
    return db_waypoint

def create_waypoint(db: Session, route_id: int, wp_in: WaypointCreate) -> Waypoint:
    db_wp = Waypoint(
        route_id=route_id,
        address=wp_in.address,
        latitud=wp_in.latitud,
        longitud=wp_in.longitud,
        order_sequence=wp_in.order_sequence,
        client_id=wp_in.client_id,
        status=wp_in.status,
        url_photo=wp_in.url_photo,
        comment=wp_in.comment
    )
    db.add(db_wp)
    db.commit()
    db.refresh(db_wp)
    return db_wp
