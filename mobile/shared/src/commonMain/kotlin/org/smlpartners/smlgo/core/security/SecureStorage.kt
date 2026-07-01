package org.smlpartners.smlgo.core.security

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Almacenamiento seguro multiplataforma usando multiplatform-settings.
 * Android → EncryptedSharedPreferences (AES256-GCM)
 * iOS     → Keychain (KeychainSettings)
 *
 * Persistido: JWT + datos básicos de sesión + roles.
 * NO persistido: contraseña, objetos de sesión complejos.
 */
class SecureStorage(private val settings: Settings) {

    companion object {
        private const val KEY_TOKEN     = "jwt_token"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_CODE = "user_code"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ROLES     = "user_roles"   // roles separados por coma
    }

    // ── Token JWT ────────────────────────────────────────────────────────
    fun saveToken(token: String)  { settings[KEY_TOKEN] = token }
    fun getToken(): String?       = settings.getStringOrNull(KEY_TOKEN)

    // ── Datos básicos del usuario en sesión ──────────────────────────────
    fun saveUserSession(id: Int, code: String, name: String) {
        settings[KEY_USER_ID]   = id
        settings[KEY_USER_CODE] = code
        settings[KEY_USER_NAME] = name
    }

    // ── Roles ─────────────────────────────────────────────────────────────
    /** Persiste la lista de roles como cadena separada por comas. */
    fun saveRoles(roles: List<String>) {
        settings[KEY_ROLES] = roles.joinToString(",")
    }

    /** Devuelve los roles guardados localmente (sin llamada a red). */
    fun getRoles(): List<String> {
        val raw = settings.getStringOrNull(KEY_ROLES) ?: return emptyList()
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    /**
     * Alias semántico de [getRoles] para verificar permisos localmente
     * sin necesidad de una llamada a red.
     */
    fun getUserRoles(): List<String> = getRoles()

    // ── Cierre de sesión ─────────────────────────────────────────────────
    fun clearSession() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_CODE)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_ROLES)
    }

    fun isLoggedIn(): Boolean  = getToken() != null

    fun getUserId(): Int?      = settings.getIntOrNull(KEY_USER_ID)
    fun getUserName(): String? = settings.getStringOrNull(KEY_USER_NAME)
    fun getUserCode(): String? = settings.getStringOrNull(KEY_USER_CODE)
}