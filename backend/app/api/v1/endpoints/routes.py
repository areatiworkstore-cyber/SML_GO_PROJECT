from app.api.v1.endpoints import routes
from app.api.v1.endpoints import routes
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status, UploadFile, File
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.route import RouteCreate, RouteUpdate, RouteResponse, WaypointUpdate, WaypointResponse, WaypointCreate
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
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
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
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return crud_route.update_route(db, db_route=route, route_in=route_in)

@router.delete("/{route_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_route(
    route_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Elimina una ruta y todas sus paradas.
    """
    route = crud_route.get_route_by_id(db, route_id=route_id)
    if not route:
        raise HTTPException(status_code=404, detail="Ruta no encontrada")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
    
    success = crud_route.delete_route(db, db_route=route)
    if not success:
        raise HTTPException(status_code=400, detail="Error al eliminar la ruta")

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
        raise HTTPException(status_code=404, detail="Parada no encontrada")
        
    # Check if waypoint belongs to a route owned by the current user
    route = crud_route.get_route_by_id(db, route_id=waypoint.route_id)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return crud_route.update_waypoint_status(db, db_waypoint=waypoint, wp_in=wp_in)

@router.post("/{route_id}/waypoints", response_model=WaypointResponse)
def create_waypoint_for_route(
    route_id: int,
    wp_in: WaypointCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Crea una nueva parada (waypoint) para una ruta existente.
    """
    route = crud_route.get_route_by_id(db, route_id=route_id)
    if not route:
        raise HTTPException(status_code=404, detail="Ruta no encontrada")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return crud_route.create_waypoint(db, route_id=route_id, wp_in=wp_in)

@router.get("/waypoints/{waypoint_id}", response_model=WaypointResponse)
def read_waypoint(
    waypoint_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene una parada (waypoint) específica.
    """
    waypoint = crud_route.get_waypoint_by_id(db, waypoint_id=waypoint_id)
    if not waypoint:
        raise HTTPException(status_code=404, detail="Waypoint no encontrado")
        
    route = crud_route.get_route_by_id(db, route_id=waypoint.route_id)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
    return waypoint

@router.get("/{route_id}/waypoints", response_model=List[WaypointResponse])
def read_waypoints(
    route_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene las paradas (waypoints) de una ruta específica.
    """
    route = crud_route.get_route_by_id(db, route_id=route_id)
    if not route:
        raise HTTPException(status_code=404, detail="Ruta no encontrada")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return crud_route.get_waypoints(db, route_id=route_id)

@router.delete("/waypoints/{waypoint_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_waypoint(
    waypoint_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Elimina una parada (waypoint) específica.
    """
    waypoint = crud_route.get_waypoint_by_id(db, waypoint_id=waypoint_id)
    if not waypoint:
        raise HTTPException(status_code=404, detail="Waypoint no encontrado")
        
    route = crud_route.get_route_by_id(db, route_id=waypoint.route_id)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
    crud_route.delete_waypoint(db, db_waypoint=waypoint)
    return None

from app.services.media_storage_service import MediaStorageService

@router.post("/waypoints/{waypoint_id}/upload-photo", response_model=WaypointResponse)
def upload_waypoint_photo(
    waypoint_id: int,
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Sube una foto de evidencia para un waypoint específico a cPanel mediante SFTP
    y almacena la ruta relativa en la base de datos.
    """
    waypoint = crud_route.get_waypoint_by_id(db, waypoint_id=waypoint_id)
    if not waypoint:
        raise HTTPException(status_code=404, detail="Waypoint no encontrado")
        
    route = crud_route.get_route_by_id(db, route_id=waypoint.route_id)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and route.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")

    # Obtener el código o ID del usuario autenticado de forma segura
    user_code = current_user.code if current_user.code else str(current_user.id)
    
    # Obtener el RUC del cliente del waypoint
    client_ruc = "desconocido"
    if waypoint.client and waypoint.client.document_number:
        client_ruc = waypoint.client.document_number

    # Subir foto por SFTP
    relative_path = MediaStorageService.upload_photo(
        file=file,
        user_code=user_code,
        client_ruc=client_ruc
    )

    # Guardar url relativa (ej. /ADM001/20601122334/2026-07-01_14-35-21.jpg)
    waypoint.url_photo = relative_path
    db.add(waypoint)
    db.commit()
    db.refresh(waypoint)

    return waypoint

