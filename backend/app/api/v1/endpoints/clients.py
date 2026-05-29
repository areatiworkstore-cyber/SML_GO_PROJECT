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
    Register a new client with GPS coordinates (latitud/longitud).
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
    Get clients list. Admin can query all or filter by user_id, sellers will only query their own clients.
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
    Get client details by ID.
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Client not found")
        
    # Check authorization (Sellers can only access their own clients)
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not enough permissions")
        
    return client


@router.put("/{client_id}", response_model=ClientResponse)
def update_client(
    client_id: int,
    client_in: ClientUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Update client details (including GPS latitud/longitud coordinates).
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Client not found")
        
    roles = [ru.role_details.role for ru in current_user.roles]
    if "ADMIN" not in roles and client.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not enough permissions")
        
    return crud_client.update_client(db, db_client=client, client_in=client_in)


@router.get("/{client_id}/maps-redirect", status_code=status.HTTP_200_OK)
def get_maps_redirect_url(
    client_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Returns a Google Maps redirect URL for the client's coordinates.
    """
    client = crud_client.get_client_by_id(db, client_id=client_id)
    if not client:
        raise HTTPException(status_code=404, detail="Client not found")
    if client.latitud is None or client.longitud is None:
        raise HTTPException(
            status_code=400,
            detail="Client coordinates (latitud/longitud) are not registered."
        )
    # Return redirect URL
    url = f"https://www.google.com/maps/search/?api=1&query={client.latitud},{client.longitud}"
    return {"url": url}
