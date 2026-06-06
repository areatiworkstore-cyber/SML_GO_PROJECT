package org.smlpartners.smlgo.core.security

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Almacenamiento seguro multiplataforma usando multiplatform-settings.
 * Android → EncryptedSharedPreferences (bajo el capó)
 * iOS     → NSUserDefaults / Keychain según configuración
 */
class SecureStorage(private val settings: Settings) {

    companion object {
        private const val KEY_TOKEN       = "jwt_token"
        private const val KEY_USER_ID     = "user_id"
        private const val KEY_USER_EMAIL  = "user_email"
        private const val KEY_USER_NAME   = "user_name"
    }

    // ── Token JWT ────────────────────────────────────────────────────
    fun saveToken(token: String)  { settings[KEY_TOKEN] = token }
    fun getToken(): String?       = settings.getStringOrNull(KEY_TOKEN)

    // ── Datos básicos del usuario en sesión ──────────────────────────
    fun saveUserSession(id: Int, email: String, name: String) {
        settings[KEY_USER_ID]    = id
        settings[KEY_USER_EMAIL] = email
        settings[KEY_USER_NAME]  = name
    }

    // ── Cierre de sesión ─────────────────────────────────────────────
    fun clearSession() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_NAME)
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun getUserId(): Int?     = settings.getIntOrNull(KEY_USER_ID)
    fun getUserName(): String? = settings.getStringOrNull(KEY_USER_NAME)
    fun getUserEmail(): String? = settings.getStringOrNull(KEY_USER_EMAIL)
}