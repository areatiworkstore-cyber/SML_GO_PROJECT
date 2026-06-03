import io
import re
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from openpyxl import load_workbook

from app.models.geographic import Department, Province, District
from app.models.master_data import DocumentType, BusinessType, ClientGroup
from app.models.client import Client
from app.models.supplier import Supplier
from app.schemas.client import ClientCreate
from app.schemas.supplier import SupplierCreate 
from app.crud import crud_client
from app.crud import crud_supplier  

# Definición técnica unificada de columnas obligatorias en snake_case
REQUIRED_COLUMNS = {
    "dia_de_visita",
    "cod_vendedor",
    "vendedor",
    "cod_cliente",
    "razon_social",
    "tipo_de_documento",
    "n_documuento",
    "direccion",
    "distrito",
    "provincia",
    "departamento",
    "tipo_de_negocio",
    "grupo_cliente",
    "celular",
}


def _normalize_header(header: str) -> str:
    """
    Normaliza los encabezados del Excel: quita tildes, caracteres especiales,
    reemplaza espacios por subguiones y lo pasa a minúsculas (snake_case).
    """
    if not header:
        return ""
    replacements = {'Á': 'A', 'É': 'E', 'Í': 'I', 'Ó': 'O', 'Ú': 'U', 'Ñ': 'N'}
    s = header.strip().upper()
    for k, v in replacements.items():
        s = s.replace(k, v)
    
    s = s.replace("N°", "N").replace("N° ", "N_")
    s = re.sub(r'[^A-Z0-9\s_]', '', s)
    
    s = re.sub(r'\s+', '_', s)
    return s.lower()


def _build_lookup_cache(db: Session) -> Dict[str, Any]:
    """Pre-carga todos los datos de referencia en memoria para evitar N+1 queries."""
    departments = db.query(Department).all()
    provinces = db.query(Province).all()
    districts = db.query(District).all()
    doc_types = db.query(DocumentType).all()
    business_types = db.query(BusinessType).all()
    client_groups = db.query(ClientGroup).all()
    suppliers = db.query(Supplier).all()
    
    # Pre-cargar solo los códigos de clientes existentes para validar la restricción UNIQUE
    # Usamos tuplas con (code,) para optimizar el consumo de memoria de la query
    existing_codes = {c[0].upper() for c in db.query(Client.code).filter(Client.code.isnot(None)).all()}

    return {
        "departments": {d.name.upper(): d for d in departments},
        "provinces": {p.name.upper(): p for p in provinces},
        "districts": {dist.name.upper(): dist for dist in districts},
        "doc_types": {dt.description.upper(): dt for dt in doc_types},
        "business_types": {bt.description.upper(): bt for bt in business_types},
        "client_groups": {cg.description.upper(): cg for cg in client_groups},
        "suppliers": {s.code.upper(): s for s in suppliers},
        "existing_client_codes": existing_codes,  # <-- Guardado en caché como un Set
    }


def _resolve_district_id(
    cache: Dict[str, Any],
    departamento: str,
    provincia: str,
    distrito: str,
) -> Optional[int]:
    """Busca el district_id de forma jerárquica. Retorna None si no existe."""
    if not departamento or not provincia or not distrito:
        return None
    try:
        dep = cache["departments"].get(departamento.upper())
        if dep is None:
            return None

        prov = cache["provinces"].get(provincia.upper())
        if prov is None or prov.department_id != dep.id:
            return None

        dist = cache["districts"].get(distrito.upper())
        if dist is None or dist.province_id != prov.id:
            return None

        return dist.id
    except Exception:
        return None


def _to_str(val) -> str:
    if val is None:
        return ""
    return str(val).strip()


def _to_float(val) -> Optional[float]:
    try:
        return float(val) if val not in (None, "") else None
    except (ValueError, TypeError):
        return None


def process_excel_import(
    file_bytes: bytes,
    user_id: int,
    db: Session,
) -> Dict[str, Any]:
    """
    Procesa el archivo Excel mapeando las columnas físicas sin subguiones
    e inserta los registros omitiendo duplicados por número de documento o código.
    """
    try:
        wb = load_workbook(io.BytesIO(file_bytes), read_only=True, data_only=True)
    except Exception:
        raise ValueError("El archivo no es un Excel válido (.xlsx).")

    ws = wb.active

    # Leer encabezados crudos y pasarlos por el normalizador a snake_case
    raw_headers = [_to_str(cell.value) for cell in next(ws.iter_rows(min_row=1, max_row=1))]
    headers = [_normalize_header(h) for h in raw_headers]
    header_index = {h: i for i, h in enumerate(headers)}

    # Validar que cumpla con el set mínimo técnico requerido
    missing = REQUIRED_COLUMNS - set(headers)
    if missing:
        friendly_missing = [m.replace("_", " ").upper() for m in missing]
        raise ValueError(f"Campos faltantes en el Excel: {', '.join(sorted(friendly_missing))}")

    # Pre-cargar cache de lookups
    cache = _build_lookup_cache(db)

    errors: List[Dict[str, Any]] = []
    created = 0
    omitted = 0

    def get_cell(row_values: list, col_name: str) -> str:
        idx = header_index.get(col_name)
        if idx is None or idx >= len(row_values):
            return ""
        return _to_str(row_values[idx])

    for row_num, row in enumerate(ws.iter_rows(min_row=2, values_only=True), start=2):
        if all(v is None for v in row):
            continue  # Fila vacía

        row_vals = list(row)

        try:
            # Extracción limpia mediante llaves técnicas normalizadas en minúsculas
            codigo_cliente = get_cell(row_vals, "cod_cliente").upper()
            nombre = get_cell(row_vals, "razon_social").upper()
            tipo_documento_txt = get_cell(row_vals, "tipo_de_documento").upper()
            num_documento = get_cell(row_vals, "n_documuento")
            direccion = get_cell(row_vals, "direccion").upper()
            distrito_txt = get_cell(row_vals, "distrito").upper()
            provincia_txt = get_cell(row_vals, "provincia").upper()
            departamento_txt = get_cell(row_vals, "departamento").upper()
            tipo_negocio_txt = get_cell(row_vals, "tipo_de_negocio").upper()
            grupo_cliente_txt = get_cell(row_vals, "grupo_cliente").upper()
            
            telefono = get_cell(row_vals, "telefono") or None
            celular = get_cell(row_vals, "celular") or None
            
            latitud = _to_float(get_cell(row_vals, "latitud"))
            longitud = _to_float(get_cell(row_vals, "longitud"))
            observacion = get_cell(row_vals, "observacion").upper()

            # Datos del Proveedor/Vendedor
            codigo_vend = get_cell(row_vals, "cod_vendedor").upper()
            nombre_completo_vendedor = get_cell(row_vals, "vendedor").upper()

            # Validar duplicados por 'codigo_cliente' contra el caché en memoria
            if codigo_cliente and codigo_cliente in cache["existing_client_codes"]:
                omitted += 1
                errors.append({
                    "fila": row_num, 
                    "error": f"Cliente con código '{codigo_cliente}' ya existe en el sistema (omitido)."
                })
                continue

            # Verificar duplicados por document_number
            existing_doc = None
            if num_documento:
                existing_doc = db.query(Client).filter(Client.document_number == num_documento).first()
            if existing_doc:
                omitted += 1
                errors.append({
                    "fila": row_num, 
                    "error": f"Cliente con documento '{num_documento}' ya existe (omitido)."
                })
                continue

            # Resolver IDs desde textos cargados en caché
            doc_type = cache["doc_types"].get(tipo_documento_txt) if tipo_documento_txt else None
            doc_type_id = doc_type.id if doc_type else None

            business_type = cache["business_types"].get(tipo_negocio_txt) if tipo_negocio_txt else None
            business_type_id = business_type.id if business_type else None

            client_group = cache["client_groups"].get(grupo_cliente_txt) if grupo_cliente_txt else None
            client_group_id = client_group.id if client_group else None

            district_id = None
            if departamento_txt or provincia_txt or distrito_txt:
                district_id = _resolve_district_id(
                    cache, departamento_txt, provincia_txt, distrito_txt
                )

            # Inferencia y creación On-The-Fly de Proveedores
            supplier_id = None
            if codigo_vend:
                supplier_obj = cache["suppliers"].get(codigo_vend)
                if not supplier_obj:
                    nuevo_prov = SupplierCreate(
                        code=codigo_vend[:50],
                        names=nombre_completo_vendedor[:255] if nombre_completo_vendedor else f"PROVEEDOR {codigo_vend}",
                        active=True
                    )
                    supplier_obj = crud_supplier.create_supplier(db, supplier_in=nuevo_prov)
                    cache["suppliers"][codigo_vend] = supplier_obj
                
                supplier_id = supplier_obj.id

            # Construir payload de la entidad
            client_data = ClientCreate(
                code=codigo_cliente[:6] if codigo_cliente else None,
                name=nombre[:50] if nombre else None,
                document_type_id=doc_type_id,
                document_number=num_documento[:11] if num_documento else None,
                address=direccion if direccion else None,
                district_id=district_id,
                business_type_id=business_type_id,
                client_group_id=client_group_id,
                cellphone=celular[:9] if celular else None,
                telephone=telefono[:9] if telefono else None,
                active=True,
                user_id=user_id,          
                supplier_id=supplier_id,  
                latitud=latitud,
                longitud=longitud,
                observation=observacion if observacion else "SIN OBSERVACIÓN",
            )

            crud_client.create_client(db, client_in=client_data)
            
            # Si se crea con éxito, agregamos el código al set temporal de caché
            # por si el mismo código viene repetido más abajo en el mismo archivo Excel
            if codigo_cliente:
                cache["existing_client_codes"].add(codigo_cliente)
                
            created += 1

        except Exception as e:
            errors.append({"fila": row_num, "error": str(e)})
            omitted += 1
            db.rollback()

    wb.close()

    total = created + omitted
    return {
        "total_registros": total,
        "creados": created,
        "omitidos": omitted,
        "errores": errors,
    }