from sqlalchemy.orm import Session
from app.models.master_data import BusinessType, DocumentType, ClientGroup, Role


# --- CRUD TIPO DE NEGOCIO ---

def get_business_type_by_id(db: Session, business_type_id: int):
    """
    Obtiene un tipo de negocio específico por su ID.
    """
    return db.query(BusinessType).filter(BusinessType.id == business_type_id).first()


def get_business_types(db: Session):
    """
    Obtiene todos los tipos de negocio del sistema.
    """
    query = db.query(BusinessType)
    return query.all()

# --- CRUD TIPO DE DOCUMENTO ---

def get_document_type_by_id(db: Session, document_type_id: int):
    """
    Obtiene un tipo de documento específico por su ID.
    """
    return db.query(DocumentType).filter(DocumentType.id == document_type_id).first()


def get_document_types(db: Session):
    """
    Obtiene todos los tipos de documento del sistema.
    """
    query = db.query(DocumentType)
    return query.all()

# --- CRUD GRUPO DE CLIENTE ---

def get_client_group_by_id(db: Session, client_group_id: int):
    """
    Obtiene un grupo de cliente específico por su ID.
    """
    return db.query(ClientGroup).filter(ClientGroup.id == client_group_id).first()


def get_client_groups(db: Session):
    """
    Obtiene todos los grupos de clientes del sistema.
    """
    query = db.query(ClientGroup)
    return query.all()

# --- CRUD ROL ---
def get_role_by_id(db: Session, role_id: int):
    """
    Obtiene un rol específico por su ID.
    """
    return db.query(Role).filter(Role.id == role_id).first()


def get_roles(db: Session):
    """
    Obtiene todos los roles del sistema.
    """
    query = db.query(Role)
    return query.all()