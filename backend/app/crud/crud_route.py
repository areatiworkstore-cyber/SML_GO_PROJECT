from datetime import datetime
from sqlalchemy.orm import Session
from app.models.route import Route, Waypoint
from app.schemas.route import RouteCreate, RouteUpdate, WaypointUpdate

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

    # Create waypoints
    for wp in route_in.waypoints:
        db_wp = Waypoint(
            route_id=db_route.id,
            address=wp.address,
            latitud=wp.latitud,
            longitud=wp.longitud,
            order_sequence=wp.order_sequence,
            client_id=wp.client_id,
            status=wp.status,
            comment=wp.comment
        )
        db.add(db_wp)
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
