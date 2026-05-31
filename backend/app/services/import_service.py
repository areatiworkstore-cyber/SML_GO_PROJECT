import io
from typing import List, Dict, Any, Optional
from sqlalchemy.orm import Session
from openpyxl import load_workbook

from app.models.geographic import Department, Province, District
from app.models.master_data import DocumentType, BusinessType, ClientGroup
from app.models.client import Client
from app.schemas.client import ClientCreate
from app.crud import crud_client

REQUIRED_COLUMNS = {
    "Dia_visita",
    "codigo_vend",
    "nombre_completo_vendedor",
    "codigo_cliente",
    "nombre o razon social del cliente",
    "tipo_documento",
    "num_documento",
    "latitud",
    "longitud",
    "direccion",
    "distrito",
    "provincia",
    "departamento",
    "tipo_negocio",
    "grupo_cliente",
    "telefono",
    "celular",
}


def _build_lookup_cache(db: Session) -> Dict[str, Any]:
    """Pre-carga todos los datos de referencia en memoria para evitar N+1 queries."""
    departments = db.query(Department).all()
    provinces = db.query(Province).all()
    districts = db.query(District).all()
    doc_types = db.query(DocumentType).all()
    business_types = db.query(BusinessType).all()
    client_groups = db.query(ClientGroup).all()

    return {
        "departments": {d.name.upper(): d for d in departments},
        "provinces": {p.name.upper(): p for p in provinces},
        "districts": {dist.name.upper(): dist for dist in districts},
        "doc_types": {dt.description.upper(): dt for dt in doc_types},
        "business_types": {bt.description.upper(): bt for bt in business_types},
        "client_groups": {cg.description.upper(): cg for cg in client_groups},
    }


def _resolve_district_id(
    cache: Dict[str, Any],
    departamento: str,
    provincia: str,
    distrito: str,
) -> Optional[int]:
    """Busca el district_id de forma jerárquica. Retorna None si no existe."""
    dep = cache["departments"].get(departamento.upper())
    if dep is None:
        raise ValueError(f"Departamento no encontrado: {departamento}")

    prov = cache["provinces"].get(provincia.upper())
    if prov is None or prov.department_id != dep.id:
        raise ValueError(f"Provincia no encontrada en {departamento}: {provincia}")

    dist = cache["districts"].get(distrito.upper())
    if dist is None or dist.province_id != prov.id:
        raise ValueError(f"Distrito no encontrado en {provincia}: {distrito}")

    return dist.id


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
    Procesa el archivo Excel y crea los clientes en la base de datos.
    Retorna un resumen: {total_registros, creados, omitidos, errores}.
    """
    try:
        wb = load_workbook(io.BytesIO(file_bytes), read_only=True, data_only=True)
    except Exception:
        raise ValueError("El archivo no es un Excel válido (.xlsx).")

    ws = wb.active

    # Leer encabezados de la primera fila
    headers = [_to_str(cell.value) for cell in next(ws.iter_rows(min_row=1, max_row=1))]
    header_index = {h: i for i, h in enumerate(headers)}

    # Validar columnas obligatorias
    missing = REQUIRED_COLUMNS - set(headers)
    if missing:
        raise ValueError(f"Campos faltantes: {', '.join(sorted(missing))}")

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
            # Extraer y normalizar valores
            codigo_cliente = _to_str(get_cell(row_vals, "codigo_cliente")).upper()
            nombre = _to_str(get_cell(row_vals, "nombre o razon social del cliente")).upper()
            tipo_documento_txt = _to_str(get_cell(row_vals, "tipo_documento")).upper()
            num_documento = _to_str(get_cell(row_vals, "num_documento"))
            direccion = _to_str(get_cell(row_vals, "direccion")).upper()
            distrito_txt = _to_str(get_cell(row_vals, "distrito")).upper()
            provincia_txt = _to_str(get_cell(row_vals, "provincia")).upper()
            departamento_txt = _to_str(get_cell(row_vals, "departamento")).upper()
            tipo_negocio_txt = _to_str(get_cell(row_vals, "tipo_negocio")).upper()
            grupo_cliente_txt = _to_str(get_cell(row_vals, "grupo_cliente")).upper()
            telefono = _to_str(get_cell(row_vals, "telefono")) or None
            celular = _to_str(get_cell(row_vals, "celular")) or None
            latitud = _to_float(get_cell(row_vals, "latitud"))
            longitud = _to_float(get_cell(row_vals, "longitud"))

            # Validaciones básicas de campos requeridos
            if not codigo_cliente:
                raise ValueError("El campo 'codigo_cliente' está vacío.")
            if not nombre:
                raise ValueError("El campo 'nombre o razon social del cliente' está vacío.")
            if not num_documento:
                raise ValueError("El campo 'num_documento' está vacío.")

            # Resolver IDs desde textos
            doc_type = cache["doc_types"].get(tipo_documento_txt)
            if doc_type is None:
                raise ValueError(f"Tipo de documento no encontrado: {tipo_documento_txt}")

            business_type = cache["business_types"].get(tipo_negocio_txt)
            if business_type is None:
                raise ValueError(f"Tipo de negocio no encontrado: {tipo_negocio_txt}")

            client_group = cache["client_groups"].get(grupo_cliente_txt)
            if client_group is None:
                raise ValueError(f"Grupo de cliente no encontrado: {grupo_cliente_txt}")

            district_id = _resolve_district_id(
                cache, departamento_txt, provincia_txt, distrito_txt
            )

            # Verificar duplicado por document_number
            existing = db.query(Client).filter(Client.document_number == num_documento).first()
            if existing:
                omitted += 1
                errors.append({"fila": row_num, "error": f"Cliente con documento '{num_documento}' ya existe (omitido)."})
                continue

            # Crear cliente
            client_data = ClientCreate(
                code=codigo_cliente[:6],
                name=nombre[:50],
                document_type_id=doc_type.id,
                document_number=num_documento[:11],
                address=direccion,
                district_id=district_id,
                business_type_id=business_type.id,
                client_group_id=client_group.id,
                cellphone=celular[:9] if celular else None,
                telephone=telefono[:9] if telefono else None,
                active=True,
                user_id=user_id,
                latitud=latitud,
                longitud=longitud,
            )

            crud_client.create_client(db, client_in=client_data)
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
