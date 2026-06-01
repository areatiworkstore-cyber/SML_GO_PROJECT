from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.route import RouteCreate, RouteUpdate, RouteResponse, WaypointUpdate, WaypointResponse
from app.crud import crud_route

router = APIRouter()

@router.post("/", response_model=RouteResponse, status_code=status.HTTP_201_CREATED)
def create_route(
    *,
    db: Session = Depends(get_db),
    route_in: RouteCreate,
    current_user: User = Depends(get_current_user)
):
    """
    Crea una nueva ruta (agenda) con multiples paradas en orden secuencial.
    """
    # 🔒 Control de accesos OWASP: Vendedores solo crean rutas para sí mismos
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles:
        route_in.user_id = current_user.id
        
    return crud_route.create_route(db, route_in=route_in)


@router.get("/", response_model=List[RouteResponse])
def read_routes(
    db: Session = Depends(get_db),
    user_id: Optional[int] = None,
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene rutas. Los vendedores ven sus propias rutas activas, los administradores pueden inspeccionar las de cualquiera.
    """
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles:
        user_id = current_user.id
        
    return crud_route.get_routes(db, user_id=user_id)


@router.get("/{route_id}", response_model=RouteResponse)
def read_route(
    route_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Recupera los detalles de una ruta específica, incluidas sus paradas.
    """
    route = crud_route.get_route_by_id(db, route_id=route_id)
    if not route:
        raise HTTPException(status_code=404, detail="Ruta no encontrada")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not enough permissions")
        
    return route


@router.put("/{route_id}", response_model=RouteResponse)
def update_route(
    route_id: int,
    route_in: RouteUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Actualiza los metadatos de una ruta (Administrador o propietario de la ruta).
    """
    route = crud_route.get_route_by_id(db, route_id=route_id)
    if not route:
        raise HTTPException(status_code=404, detail="Ruta no encontrada")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not enough permissions")
        
    return crud_route.update_route(db, db_route=route, route_in=route_in)


@router.put("/waypoints/{waypoint_id}", response_model=WaypointResponse)
def update_waypoint_status(
    waypoint_id: int,
    wp_in: WaypointUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Actualiza el estado de una parada (por ejemplo, VISITA, CANCELADA), registrando la fecha y hora de la visita y los comentarios.
    (Usado por los vendedores en la aplicación móvil para gestionar las visitas).
    """
    waypoint = crud_route.get_waypoint_by_id(db, waypoint_id=waypoint_id)
    if not waypoint:
        raise HTTPException(status_code=404, detail="Waypoint not found")
        
    # Check if waypoint belongs to a route owned by the current user
    route = crud_route.get_route_by_id(db, route_id=waypoint.route_id)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not enough permissions")
        
    return crud_route.update_waypoint_status(db, db_waypoint=waypoint, wp_in=wp_in)
