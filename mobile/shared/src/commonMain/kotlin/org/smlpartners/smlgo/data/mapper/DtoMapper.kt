package org.smlpartners.smlgo.data.mapper

import org.smlpartners.smlgo.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDateTime
import org.smlpartners.smlgo.data.remote.dto.*

// ── Auth ─────────────────────────────────────────────────────────────────

fun TokenResponseDto.toDomain(): String = accessToken

// ── Master data ──────────────────────────────────────────────────────────

fun DocumentTypeDto.toDomain() = DocumentType(id = id, description = description)
fun BusinessTypeDto.toDomain() = BusinessType(id = id, description = description)
fun ClientGroupDto.toDomain()  = ClientGroup(id = id, description = description)
fun SupplierDto.toDomain()     = Supplier(id = id, code = code, names = names, active = active)
fun RoleDto.toDomain()         = Role(id = id, role = role)

// ── Geography ────────────────────────────────────────────────────────────

fun DepartmentDto.toDomain() = Department(id = id, name = name, active = active)
fun ProvinceDto.toDomain()   = Province(id = id, name = name, active = active, departmentId = departmentId)
fun DistrictDto.toDomain()   = District(id = id, name = name, active = active, provinceId = provinceId)

// ── User ─────────────────────────────────────────────────────────────────

fun UserDto.toDomain() = User(
    id             = id,
    code           = code,
    firstName      = firstName,
    secondName     = secondName,
    firstSurname   = firstSurname,
    secondSurname  = secondSurname,
    documentType   = documentType?.toDomain(),
    documentNumber = documentNumber,
    cellphone      = cellphone,
    email          = email,
    roles          = roles.mapNotNull { it.roleDetails?.toDomain() }
)

// ── Client ───────────────────────────────────────────────────────────────

fun ClientDto.toDomain() = Client(
    id             = id,
    code           = code,
    name           = name,
    documentType   = documentType?.toDomain(),
    documentNumber = documentNumber,
    address        = address,
    district       = district?.toDomain(),
    businessType   = businessType?.toDomain(),
    clientGroup    = clientGroup?.toDomain(),
    cellphone      = cellphone,
    telephone      = telephone,
    active         = active,
    latitude       = latitude,
    longitude      = longitude,
    observation    = observation,
    supplier       = supplier?.toDomain()
)

// Client → Request DTO (para crear/editar)
fun Client.toRequestDto() = ClientRequestDto(
    code           = code,
    name           = name,
    documentTypeId = documentType?.id,
    documentNumber = documentNumber,
    address        = address,
    districtId     = district?.id,
    businessTypeId = businessType?.id,
    clientGroupId  = clientGroup?.id,
    cellphone      = cellphone,
    telephone      = telephone,
    latitude       = latitude,
    longitude      = longitude,
    observation    = observation,
    supplierId     = supplier?.id
)

// ── Waypoint ─────────────────────────────────────────────────────────────

fun WaypointDto.toDomain() = Waypoint(
    id            = id,
    routeId       = routeId,
    address       = address,
    latitude      = latitude,
    longitude     = longitude,
    orderSequence = orderSequence,
    clientId      = client?.id!!,
    clientName    = client.name,
    status        = WaypointStatus.from(status),
    visitedAt     = visitedAt?.toLocalDateTime(),
    comment       = comment
)

// ── Route ────────────────────────────────────────────────────────────────

fun RouteDto.toDomain() = Route(
    id            = id,
    name          = name,
    scheduledDate = LocalDate.parse(scheduledDate),
    active        = active,
    waypoints     = waypoints.map { it.toDomain() }
)

// ── ClientSchedule ───────────────────────────────────────────────────────

fun ClientScheduleDto.toDomain() = ClientSchedule(
    id          = id,
    client      = client.toDomain(),
    day         = LocalDate.parse(day),
    startTime   = LocalTime.parse(startTime),
    observation = observation,
    active      = active
)

// ── Helpers de fecha ─────────────────────────────────────────────────────

/**
 * El backend devuelve formato ISO: "2024-01-15T10:30:00"
 * LocalDateTime.parse lo maneja directamente.
 */
private fun String.toLocalDateTime(): LocalDateTime =
    LocalDateTime.parse(this.substringBefore("."))  // elimina microsegundos si los hay