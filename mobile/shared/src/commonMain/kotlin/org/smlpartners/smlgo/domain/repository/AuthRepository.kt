package org.smlpartners.smlgo.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.smlpartners.smlgo.core.network.ApiResult
import org.smlpartners.smlgo.domain.model.Profile
import org.smlpartners.smlgo.domain.model.User

interface AuthRepository {
    val isLoggedInFlow: StateFlow<Boolean>
    suspend fun login(username: String, password: String): ApiResult<Profile>
    suspend fun logout()
    suspend fun register(username: String, password: String): ApiResult<User>
    suspend fun updateUser(id: Int, user: User): ApiResult<User>
    suspend fun getFullUser(): ApiResult<User>
    suspend fun getActiveUsers(): ApiResult<List<User>>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): Int?
}