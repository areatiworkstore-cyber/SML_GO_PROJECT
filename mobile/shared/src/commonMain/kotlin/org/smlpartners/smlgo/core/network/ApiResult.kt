package org.smlpartners.smlgo.core.network

/**
 * Wrapper genérico para todas las respuestas de la API.
 * Evita lanzar excepciones en los repositorios.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T)         : ApiResult<T>()
    data class Error(val exception: ApiError)  : ApiResult<Nothing>()
}

sealed class ApiError(open val message: String) {
    data class NetworkError(override val message: String)    : ApiError(message)  // sin conexión
    data class HttpError(val code: Int, override val message: String) : ApiError(message)  // 4xx, 5xx
    data class SerializationError(override val message: String) : ApiError(message)
    data class UnknownError(override val message: String)    : ApiError(message)
}

/** Helper para usar en repositorios sin try/catch repetitivo */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: io.ktor.client.plugins.ClientRequestException) {
        ApiResult.Error(ApiError.HttpError(e.response.status.value, e.message ?: "Error del cliente"))
    } catch (e: io.ktor.client.plugins.ServerResponseException) {
        ApiResult.Error(ApiError.HttpError(e.response.status.value, e.message ?: "Error del servidor"))
    } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
        ApiResult.Error(ApiError.NetworkError("Tiempo de conexión agotado"))
    } catch (e: io.ktor.client.network.sockets.SocketTimeoutException) {
        ApiResult.Error(ApiError.NetworkError("Tiempo de lectura agotado"))
    } catch (e: kotlinx.serialization.SerializationException) {
        ApiResult.Error(ApiError.SerializationError("Error al procesar la respuesta"))
    } catch (e: Exception) {
        ApiResult.Error(ApiError.UnknownError(e.message ?: "Error desconocido"))
    }
}