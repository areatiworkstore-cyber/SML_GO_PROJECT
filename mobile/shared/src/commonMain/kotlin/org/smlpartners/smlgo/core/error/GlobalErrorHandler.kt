package org.smlpartners.smlgo.core.error

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Canal global de errores.
 * Cualquier ViewModel puede emitir un error aquí.
 * El GlobalErrorScreen los escucha y los muestra.
 */
object GlobalErrorHandler {

    private val _errors = MutableSharedFlow<AppError>(extraBufferCapacity = 1)
    val errors: SharedFlow<AppError> = _errors.asSharedFlow()

    fun emit(error: AppError) {
        _errors.tryEmit(error)
    }

    fun emit(apiError: org.smlpartners.smlgo.core.network.ApiError) {
        _errors.tryEmit(apiError.toAppError())
    }
}