from sqlalchemy.orm import Session
from app.models.client import Client
from app.schemas.client import ClientCreate, ClientUpdate

def get_client_by_id(db: Session, client_id: int):
    return db.query(Client).filter(Client.id == client_id).first()

def get_client_by_code_and_user(db: Session, code: str, user_id: int):
    return db.query(Client).filter(Client.code == code, Client.user_id == user_id).first()

def get_clients(db: Session, skip: int = 0, limit: int = 100, user_id: int = None):
    query = db.query(Client)
    if user_id is not None:
        query = query.filter(Client.user_id == user_id)
    return query.offset(skip).limit(limit).all()

def create_client(db: Session, client_in: ClientCreate) -> Client:
    db_client = Client(
        code=client_in.code,
        name=client_in.name,
        document_type_id=client_in.document_type_id,
        document_number=client_in.document_number,
        address=client_in.address,
        district_id=client_in.district_id,
        business_type_id=client_in.business_type_id,
        client_group_id=client_in.client_group_id,
        cellphone=client_in.cellphone,
        telephone=client_in.telephone,
        active=client_in.active,
        user_id=client_in.user_id,
        latitud=client_in.latitud,
        longitud=client_in.longitud,
        observation=client_in.observation
    )
    db.add(db_client)
    db.commit()
    db.refresh(db_client)
    return db_client

def update_client(db: Session, db_client: Client, client_in: ClientUpdate) -> Client:
    update_data = client_in.model_dump(exclude_unset=True)
    for field in update_data:
        setattr(db_client, field, update_data[field])
    db.add(db_client)
    db.commit()
    db.refresh(db_client)
    return db_client
