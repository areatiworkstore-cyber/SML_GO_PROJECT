package org.smlpartners.smlgo.core.error

import org.smlpartners.smlgo.core.network.ApiError

/**
 * Convierte cualquier ApiError o excepción en un AppError
 * con mensaje amigable para el usuario.
 */

fun ApiError.toAppError(): AppError = when (this) {
    is ApiError.NetworkError -> {
        when {
            message.contains("timeout", ignoreCase = true) -> AppError.Timeout
            message.contains("connect", ignoreCase = true) -> AppError.NoInternet
            else                                            -> AppError.NoInternet
        }
    }
    is ApiError.HttpError -> {
        when (code) {
            401  -> AppError.Unauthorized
            403  -> AppError.Forbidden
            404  -> AppError.NotFound
            422  -> AppError.ValidationError(message)
            503  -> AppError.ServerUnavailable
            in 500..599 -> AppError.ServerError(code, message)
            else -> AppError.Unknown(message)
        }
    }
    is ApiError.SerializationError -> AppError.SerializationError(message)
    is ApiError.UnknownError       -> AppError.Unknown(message)
}

/**
 * Convierte cualquier Throwable en AppError.
 * Usado como fallback en catch genéricos.
 */
fun Throwable.toAppError(): AppError = when {
    message?.contains("timeout", ignoreCase = true) == true    -> AppError.Timeout
    message?.contains("connect", ignoreCase = true) == true    -> AppError.NoInternet
    message?.contains("serializ", ignoreCase = true) == true   -> AppError.SerializationError(message ?: "")
    message?.contains("unauthorized", ignoreCase = true) == true -> AppError.Unauthorized
    else -> AppError.Unknown(message ?: "Error desconocido")
}