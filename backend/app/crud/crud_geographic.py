from sqlalchemy.orm import Session
from app.models.geographic import Department, Province, District

# --- CRUD DEPARTMENTS ---

def get_department_by_id(db: Session, department_id: int):
    """
    Obtiene un departamento específico por su ID.
    """
    return db.query(Department).filter(Department.id == department_id).first()


def get_departments(db: Session, active_only: bool = True):
    """
    Obtiene todos los departamentos. Por defecto, solo los activos.
    """
    query = db.query(Department)
    if active_only:
        query = query.filter(Department.active == True)
    return query.all()


# --- CRUD PROVINCES ---

def get_province_by_id(db: Session, province_id: int):
    """
    Obtiene una provincia específica por su ID.
    """
    return db.query(Province).filter(Province.id == province_id).first()


def get_provinces(db: Session, active_only: bool = True):
    """
    Obtiene todas las provincias del sistema.
    """
    query = db.query(Province)
    if active_only:
        query = query.filter(Province.active == True)
    return query.all()


def get_provinces_by_department(db: Session, department_id: int, active_only: bool = True):
    """
    Obtiene todas las provincias asociadas a un departamento específico.
    Útil si en el futuro decides filtrar directamente desde la base de datos por Query Params.
    """
    query = db.query(Province).filter(Province.department_id == department_id)
    if active_only:
        query = query.filter(Province.active == True)
    return query.all()


# --- CRUD DISTRICTS ---

def get_district_by_id(db: Session, district_id: int):
    """
    Obtiene un distrito específico por su ID.
    """
    return db.query(District).filter(District.id == district_id).first()


def get_districts(db: Session, active_only: bool = True):
    """
    Obtiene todos los distritos del sistema.
    """
    query = db.query(District)
    if active_only:
        query = query.filter(District.active == True)
    return query.all()


def get_districts_by_province(db: Session, province_id: int, active_only: bool = True):
    """
    Obtiene todos los distritos asociados a una provincia específica.
    """
    query = db.query(District).filter(District.province_id == province_id)
    if active_only:
        query = query.filter(District.active == True)
    return query.all()