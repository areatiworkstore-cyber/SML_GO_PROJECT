package org.smlpartners.smlgo.domain.repository

import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): ApiResult<User>
    suspend fun logout()
    suspend fun register(username: String, password: String): ApiResult<User>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): Int?
}