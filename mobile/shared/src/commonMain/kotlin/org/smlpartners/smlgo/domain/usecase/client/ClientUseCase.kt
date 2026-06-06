package org.smlpartners.smlgo.domain.usecase.client

import org.smlpartners.smlgo.core.network.ApiError
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Client
import org.smlpartners.smlgo.domain.repository.ClientRepository

class GetClientsUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(): ApiResult<List<Client>> =
        repository.getClients()
}

class GetClientByIdUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(id: Int): ApiResult<Client> =
        repository.getClientById(id)
}

class GetClientsWithLocationUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(): ApiResult<List<Client>> =
        repository.getClientsWithLocation()
}

class CreateClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(client: Client): ApiResult<Client> {
        val errors = validateClient(client)
        if (errors.isNotEmpty()) return ApiResult.Error(
            ApiError.UnknownError(errors.joinToString(", "))
        )
        return repository.createClient(client)
    }
}

class UpdateClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(id: Int, client: Client): ApiResult<Client> {
        val errors = validateClient(client)
        if (errors.isNotEmpty()) return ApiResult.Error(
            ApiError.UnknownError(errors.joinToString(", "))
        )
        return repository.updateClient(id, client)
    }
}

private fun validateClient(client: Client): List<String> = buildList {
    if (client.name.isNullOrBlank())
        add("El nombre del cliente es obligatorio")
    if (client.cellphone != null && client.cellphone.length != 9)
        add("El celular debe tener 9 dígitos")
    if (client.documentNumber != null && client.documentNumber.length > 11)
        add("El número de documento no puede superar 11 caracteres")
    if (client.latitude != null && (client.latitude < -90 || client.latitude > 90))
        add("Latitud inválida")
    if (client.longitude != null && (client.longitude < -180 || client.longitude > 180))
        add("Longitud inválida")
}