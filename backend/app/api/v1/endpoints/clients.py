from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.client import ClientCreate, ClientUpdate, ClientResponse
from app.crud import crud_client

router = APIRouter()

@router.post("/", response_model=ClientResponse, status_code=status.HTTP_201_CREATED)
def create_client(
    *,
    db: Session = Depends(get_db),
    client_in: ClientCreate,
    current_user: User = Depends(get_current_user)
):
    """
    Registra un nuevo cliente con coordenadas GPS (latitud/longitud).
    """
    # Check if client code is already registered for this seller/user
    db_client = crud_client.get_client_by_code_and_user(
        db, code=client_in.code, user_id=client_in.user_id
    )
    if db_client:
        raise HTTPException(
            status_code=400,
            detail="Código de cliente ya registrado."
        )
    return crud_client.create_client(db, client_in=client_in)


@router.get("/", response_model=List[ClientResponse])
def read_clients(
    db: Session = Depends(get_db),
    user_id: Optional[int] = None,
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene la lista de clientes. Admin puede consultar todos o filtrar por user_id, 
    los vendedores solo consultarán sus propios clientes.
    """
    # Check if user is seller (VENDEDOR). If so, force filter to their own ID.
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles:
        user_id = current_user.id
        
    return crud_client.get_clients(db, user_id=user_id)


@router.get("/{client_id}", response_model=ClientResponse)
def read_client(
    client_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Obtiene los detalles de un cliente por ID.
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
        
    # Check authorization (Sellers can only access their own clients)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return client


@router.put("/{client_id}", response_model=ClientResponse)
def update_client(
    client_id: int,
    client_in: ClientUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Actualiza los detalles de un cliente (incluyendo coordenadas GPS latitud/longitud).
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    return crud_client.update_client(db, db_client=client, client_in=client_in)


@router.get("/{client_id}/maps-redirect", status_code=status.HTTP_200_OK)
def get_maps_redirect_url(
    client_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Retorna una URL de redirección a Google Maps para las coordenadas del cliente.
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
    if client.latitud is None or client.longitud is None:
        raise HTTPException(
            status_code=400,
            detail="Las coordenadas del cliente (latitud/longitud) no están registradas."
        )
    # Retorna URL de redirección a Google Maps
    url = f"https://www.google.com/maps/search/?api=1&query={client.latitud},{client.longitud}"
    return {"url": url}

@router.delete("/{client_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_client(
    client_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Desactiva/elimina un cliente por ID.
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Cliente no encontrado")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="No tienes permisos para realizar esta acción")
        
    crud_client.delete_client(db, client_id=client_id)
    return None