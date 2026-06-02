from sqlalchemy.orm import Session
from app.models.supplier import Supplier
from app.schemas.supplier import SupplierCreate, SupplierUpdate


def get_supplier_by_id(db: Session, supplier_id: int):
    return db.query(Supplier).filter(Supplier.id == supplier_id).first()


def get_supplier_by_code(db: Session, code: str):
    return db.query(Supplier).filter(Supplier.code == code).first()


def get_suppliers(db: Session, skip: int = 0, limit: int = 100, active: bool = None):
    query = db.query(Supplier)
    if active is not None:
        query = query.filter(Supplier.active == active)
    return query.offset(skip).limit(limit).all()


def create_supplier(db: Session, supplier_in: SupplierCreate) -> Supplier:
    db_supplier = Supplier(
        code=supplier_in.code,
        names=supplier_in.names,
        active=supplier_in.active if supplier_in.active is not None else True,
    )
    db.add(db_supplier)
    db.commit()
    db.refresh(db_supplier)
    return db_supplier


def get_or_create_supplier(db: Session, code: str, names: str) -> Supplier:
    """Busca el proveedor por código; si no existe, lo crea automáticamente."""
    supplier = get_supplier_by_code(db, code=code)
    if supplier is None:
        supplier = create_supplier(db, SupplierCreate(code=code, names=names, active=True))
    return supplier


def update_supplier(db: Session, db_supplier: Supplier, supplier_in: SupplierUpdate) -> Supplier:
    update_data = supplier_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_supplier, field, value)
    db.add(db_supplier)
    db.commit()
    db.refresh(db_supplier)
    return db_supplier


def delete_supplier(db: Session, supplier_id: int):
    db_supplier = get_supplier_by_id(db, supplier_id)
    if db_supplier:
        db_supplier.active = False
        db.add(db_supplier)
        db.commit()
        db.refresh(db_supplier)
    return db_supplier
