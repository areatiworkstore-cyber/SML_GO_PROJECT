package org.smlpartners.smlgo.data.mapper

import org.smlpartners.smlgo.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDateTime
import org.smlpartners.smlgo.data.remote.dto.*

// ── Auth ─────────────────────────────────────────────────────────────────

fun TokenResponseDto.toDomain(): String? = accessToken

// ── Master data ──────────────────────────────────────────────────────────

fun DocumentTypeDto.toDomain() = DocumentType(id = id, description = description)
fun BusinessTypeDto.toDomain() = BusinessType(id = id, description = description)
fun ClientGroupDto.toDomain()  = ClientGroup(id = id, description = description)
fun RoleDto.toDomain()         = Role(id = id, role = role)
fun RoleUserDto.toDomain() = Role(
    id   = roleDetails?.id   ?: roleId,
    role = roleDetails?.role ?: "Rol $roleId"
)

// ── Geography ────────────────────────────────────────────────────────────

fun DepartmentDto.toDomain() = Department(id = id, name = name, active = active)
fun ProvinceDto.toDomain()   = Province(id = id, name = name, active = active, departmentId = departmentId)
fun DistrictDto.toDomain()   = District(id = id, name = name, active = active, provinceId = provinceId)

// ── User ─────────────────────────────────────────────────────────────────

fun UserDto.toDomain() = User(
    id             = id,
    code           = code,
    firstName      = firstName ?: "User",
    secondName     = secondName ?: "",
    firstSurname   = firstSurname ?: "SML",
    secondSurname  = secondSurname ?: "",
    documentType   = documentType?.toDomain(),
    documentNumber = documentNumber,
    cellphone      = cellphone,
    email          = email,
    roles          = roles.map { it.toDomain() },
    active         = true
)

fun User.toUpdateDto(password: String? = null) = UserUpdateDto(
    firstName      = firstName,
    secondName     = secondName,
    firstSurname   = firstSurname,
    secondSurname  = secondSurname,
    documentTypeId = documentType?.id,
    documentNumber = documentNumber,
    cellphone      = cellphone,
    email          = email,
    password       = password,
    active         = true,
    roleIds        = null
)

fun MyProfileDto.toDomainUser() = User(
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
    roles          = roles.map { it.toDomain() },
    active         = true
)

// ── Client DTO → Domain Model ─────────────────────────────────────────────

fun ClientDto.toDomain() = Client(
    id             = id,
    code           = code,
    name           = name,
    documentType   = documentTypeId?.let { DocumentType(id = it, description = "") },
    documentNumber = documentNumber,
    address        = address,
    district       = districtId?.let { District(id = it, name = "", active = true, provinceId = 0) },
    businessType   = businessTypeId?.let { BusinessType(id = it, description = "") },
    clientGroup    = clientGroupId?.let { ClientGroup(id = it, description = "") },
    cellphone      = cellphone,
    telephone      = telephone,
    active         = active,
    latitude       = latitude,
    longitude      = longitude,
    observation    = observation,
)

// ── Domain Model → Request DTO ────────────────────────────────────────────
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
    active         = active,
    latitude       = latitude,
    longitude      = longitude,
    observation    = observation,
)

// ── Next-Code Client ────────────────────────────────────────────────────────────────
fun NextCodeDto.toDomain() = NextCode(nextCode = code)

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

fun Waypoint.toCreateDto() = WaypointCreateDto(
    address       = address,
    latitude      = latitude,
    longitude     = longitude,
    orderSequence = orderSequence,
    clientId      = clientId,
    status        = status.name,
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

fun Route.toUpdateDto() = RouteUpdateDto(
    name          = name,
    scheduledDate = scheduledDate.toString(),
    active        = active
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

fun ClientSchedule.toRequestDto() = ClientScheduleRequestDto(
    clientId    = client.id,
    day         = day.toString(),
    startTime   = startTime.toString(),
    observation = observation
)

// ── Helpers de fecha ─────────────────────────────────────────────────────

/**
 * El backend devuelve formato ISO: "2024-01-15T10:30:00"
 * LocalDateTime.parse lo maneja directamente.
 */
private fun String.toLocalDateTime(): LocalDateTime =
    LocalDateTime.parse(this.substringBefore("."))  // elimina microsegundos si los hay