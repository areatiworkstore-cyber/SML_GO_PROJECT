from sqlalchemy.orm import Session
from app.core.database import SessionLocal, engine, Base
from app.core.security import get_password_hash
from app.models.master_data import Role, DocumentType, BusinessType, ClientGroup
from app.models.geographic import Department, Province, District
from app.models.user import User, RoleUser

def init_db():
    # 1. Create tables if they do not exist
    print("Creando tablas en la base de datos...")
    Base.metadata.create_all(bind=engine)
    
    db = SessionLocal()
    try:
        # 2. Seed Roles
        print("Registrando roles...")
        roles = ["ADMIN", "VENDEDOR"]
        for role_name in roles:
            exists = db.query(Role).filter(Role.role == role_name).first()
            if not exists:
                db.add(Role(role=role_name))
        db.commit()

        # 3. Seed Document Types
        print("Registrando tipos de documento...")
        doc_types = ["DNI", "RUC"]
        for doc_desc in doc_types:
            exists = db.query(DocumentType).filter(DocumentType.description == doc_desc).first()
            if not exists:
                db.add(DocumentType(description=doc_desc))
        db.commit()

        # 4. Seed Business Types
        print("Registrando tipos de negocio...")
        biz_types = ["LUBRICENTRO", "TALLER MECANICO", "FERRETERIA", "INSTALACION ELECTRICA", "INSTALACION GASISTA", "PLOMERIA", "OTRO"]
        for biz_desc in biz_types:
            exists = db.query(BusinessType).filter(BusinessType.description == biz_desc).first()
            if not exists:
                db.add(BusinessType(description=biz_desc))
        db.commit()

        # 5. Seed Client Groups
        print("Registrando grupos de clientes...")
        groups = ["B2B", "B2C", "B2G", "C2C"]
        for grp in groups:
            exists = db.query(ClientGroup).filter(ClientGroup.description == grp).first()
            if not exists:
                db.add(ClientGroup(description=grp))
        db.commit()

        # 6. Seed UBIGEO master data (sample)
        print("Registrando departamentos y provincias de ejemplo...")
        dept = db.query(Department).filter(Department.name == "LIMA").first()
        if not dept:
            dept = Department(name="LIMA", active=True)
            db.add(dept)
            db.commit()
            db.refresh(dept)
            
        prov = db.query(Province).filter(Province.name == "LIMA", Province.department_id == dept.id).first()
        if not prov:
            prov = Province(name="LIMA", active=True, department_id=dept.id)
            db.add(prov)
            db.commit()
            db.refresh(prov)
            
        dist = db.query(District).filter(District.name == "MIRAFLORES", District.province_id == prov.id).first()
        if not dist:
            dist = District(name="MIRAFLORES", active=True, province_id=prov.id)
            db.add(dist)
            db.commit()

        # 7. Seed Default Admin User
        print("Registrando usuario Administrador de prueba...")
        admin_exists = db.query(User).filter(User.code == "ADM001").first()
        if not admin_exists:
            hashed_pw = get_password_hash("admin123")
            admin_user = User(
                code="ADM001",
                first_name="Admin",
                second_name="Sistemas",
                first_surname="San",
                second_surname="Marlu",
                document_type_id=1,  # DNI
                document_number="11111111",
                cellphone="999999999",
                email="admin@sanmarlu.com.pe",
                password=hashed_pw
            )
            db.add(admin_user)
            db.commit()
            db.refresh(admin_user)
            
            # Link Admin role
            admin_role = db.query(Role).filter(Role.role == "ADMIN").first()
            if admin_role:
                db.add(RoleUser(user_id=admin_user.id, role_id=admin_role.id))
                db.commit()

        # 8. Seed Default Seller User
        print("Registrando usuario Vendedor de prueba...")
        seller_exists = db.query(User).filter(User.code == "VEN001").first()
        if not seller_exists:
            hashed_pw = get_password_hash("vendedor123")
            seller_user = User(
                code="VEN001",
                first_name="Juan",
                second_name="Carlos",
                first_surname="Perez",
                second_surname="Gomez",
                document_type_id=1,  # DNI
                document_number="22222222",
                cellphone="988888888",
                email="juan.perez@sanmarlu.com.pe",
                password=hashed_pw
            )
            db.add(seller_user)
            db.commit()
            db.refresh(seller_user)
            
            # Link Vendedor role
            vendedor_role = db.query(Role).filter(Role.role == "VENDEDOR").first()
            if vendedor_role:
                db.add(RoleUser(user_id=seller_user.id, role_id=vendedor_role.id))
                db.commit()

        print("¡Base de datos inicializada y poblada exitosamente!")
    except Exception as e:
        print(f"Error al inicializar base de datos: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    init_db()
