package org.smlpartners.smlgo.core.error

/**
 * Errores globales de la app clasificados por tipo.
 * Se mapean desde ApiError, excepciones de serialización,
 * errores de red y errores inesperados.
 */

sealed class AppError {

    // ── Red ───────────────────────────────────────────────────────────
    object NoInternet          : AppError()
    object Timeout             : AppError()
    object ServerUnavailable   : AppError()

    // ── Autenticación ─────────────────────────────────────────────────
    object Unauthorized        : AppError()
    object SessionExpired      : AppError()

    // ── Servidor ──────────────────────────────────────────────────────
    data class ServerError(val code: Int, val detail: String) : AppError()
    object NotFound            : AppError()
    object Forbidden           : AppError()

    // ── Cliente ───────────────────────────────────────────────────────
    data class ValidationError(val message: String) : AppError()
    data class SerializationError(val detail: String) : AppError()

    // ── Desconocido ───────────────────────────────────────────────────
    data class Unknown(val detail: String) : AppError()

    // ── Mensaje amigable para el usuario ──────────────────────────────
    fun toUserMessage(): String = when (this) {
        is NoInternet        -> "Sin conexión a internet. Verifica tu red e intenta nuevamente."
        is Timeout           -> "La conexión tardó demasiado. Intenta nuevamente."
        is ServerUnavailable -> "El servidor no está disponible. Intenta más tarde."
        is Unauthorized      -> "Credenciales incorrectas. Verifica tu correo y contraseña."
        is SessionExpired    -> "Tu sesión ha expirado. Inicia sesión nuevamente."
        is NotFound          -> "El recurso solicitado no fue encontrado."
        is Forbidden         -> "No tienes permisos para realizar esta acción."
        is ServerError       -> "Error del servidor ($code). Intenta más tarde."
        is ValidationError   -> message
        is SerializationError-> "Error al procesar la respuesta del servidor."
        is Unknown           -> "Ocurrió un error inesperado. Intenta nuevamente."
    }

    // ── Ícono sugerido para la UI ─────────────────────────────────────
    fun toIcon(): ErrorIcon = when (this) {
        is NoInternet        -> ErrorIcon.NO_INTERNET
        is Timeout           -> ErrorIcon.TIMEOUT
        is ServerUnavailable -> ErrorIcon.SERVER
        is Unauthorized      -> ErrorIcon.AUTH
        is SessionExpired    -> ErrorIcon.AUTH
        is NotFound          -> ErrorIcon.NOT_FOUND
        is Forbidden         -> ErrorIcon.FORBIDDEN
        is ServerError       -> ErrorIcon.SERVER
        is ValidationError   -> ErrorIcon.VALIDATION
        is SerializationError-> ErrorIcon.SERVER
        is Unknown           -> ErrorIcon.UNKNOWN
    }

    // ── Si es recuperable (muestra reintentar) ────────────────────────
    fun isRetryable(): Boolean = when (this) {
        is NoInternet        -> true
        is Timeout           -> true
        is ServerUnavailable -> true
        is ServerError       -> code >= 500
        else                 -> false
    }

    // ── Si requiere cerrar sesión ─────────────────────────────────────
    fun requiresLogout(): Boolean =
        this is SessionExpired || this is Unauthorized

    enum class ErrorIcon {
        NO_INTERNET,
        TIMEOUT,
        SERVER,
        AUTH,
        NOT_FOUND,
        FORBIDDEN,
        VALIDATION,
        UNKNOWN
    }
}