package org.smlpartners.smlgo.core.security

import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Implementación de [Settings] respaldada por el Keychain de iOS.
 * Cada entrada es un ítem `kSecClassGenericPassword` identificado por
 * service + account (key).
 *
 * Todos los valores se almacenan como UTF-8 String; los tipos numéricos
 * y booleanos se serializan a/desde String.
 */
@OptIn(ExperimentalForeignApi::class)
private class KeychainSettings(private val service: String) : Settings {

    // ── Helpers internos ─────────────────────────────────────────────────

    private fun baseQuery(key: String) = buildMap<CFStringRef?, Any?> {
        put(kSecClass,       kSecClassGenericPassword)
        put(kSecAttrService, service)
        put(kSecAttrAccount, key)
    }

    private fun readRaw(key: String): String? = memScoped {
        val query = CFDictionaryCreateMutable(null, 0, null, null)!!
        CFDictionaryAddValue(query, kSecClass,       kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetainHack(service))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetainHack(key))
        CFDictionaryAddValue(query, kSecReturnData,  kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit,  kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)

        if (status == errSecSuccess) {
            val data = result.value as? NSData ?: return@memScoped null
            NSString.create(data, NSUTF8StringEncoding) as? String
        } else null
    }

    private fun writeRaw(key: String, value: String) {
        val nsData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val existing = readRaw(key)

        if (existing != null) {
            memScoped {
                val query = CFDictionaryCreateMutable(null, 0, null, null)!!
                CFDictionaryAddValue(query, kSecClass,       kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetainHack(service))
                CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetainHack(key))

                val update = CFDictionaryCreateMutable(null, 0, null, null)!!
                CFDictionaryAddValue(update, kSecValueData, nsData)
                SecItemUpdate(query, update)
            }
        } else {
            memScoped {
                val addQuery = CFDictionaryCreateMutable(null, 0, null, null)!!
                CFDictionaryAddValue(addQuery, kSecClass,       kSecClassGenericPassword)
                CFDictionaryAddValue(addQuery, kSecAttrService, CFBridgingRetainHack(service))
                CFDictionaryAddValue(addQuery, kSecAttrAccount, CFBridgingRetainHack(key))
                CFDictionaryAddValue(addQuery, kSecValueData,   nsData)
                SecItemAdd(addQuery, null)
            }
        }
    }

    private fun deleteRaw(key: String) {
        memScoped {
            val query = CFDictionaryCreateMutable(null, 0, null, null)!!
            CFDictionaryAddValue(query, kSecClass,       kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetainHack(service))
            CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetainHack(key))
            SecItemDelete(query)
        }
    }

    /** Convierte NSString a CFTypeRef sin gestión de memoria Kotlin-side. */
    private fun CFBridgingRetainHack(s: String): CFStringRef? =
        @Suppress("UNCHECKED_CAST")
        (s as NSString) as? CFStringRef

    // ── Settings interface ────────────────────────────────────────────────

    override val keys: Set<String> get() = emptySet()   // no enumeración en Keychain
    override val size: Int get() = 0

    override fun clear() { /* no-op: las claves se borran individualmente */ }

    override fun remove(key: String) = deleteRaw(key)

    override fun hasKey(key: String): Boolean = readRaw(key) != null

    // String
    override fun putString(key: String, value: String) = writeRaw(key, value)
    override fun getString(key: String, defaultValue: String): String =
        readRaw(key) ?: defaultValue
    override fun getStringOrNull(key: String): String? = readRaw(key)

    // Int
    override fun putInt(key: String, value: Int) = writeRaw(key, value.toString())
    override fun getInt(key: String, defaultValue: Int): Int =
        readRaw(key)?.toIntOrNull() ?: defaultValue
    override fun getIntOrNull(key: String): Int? = readRaw(key)?.toIntOrNull()

    // Long
    override fun putLong(key: String, value: Long) = writeRaw(key, value.toString())
    override fun getLong(key: String, defaultValue: Long): Long =
        readRaw(key)?.toLongOrNull() ?: defaultValue
    override fun getLongOrNull(key: String): Long? = readRaw(key)?.toLongOrNull()

    // Float
    override fun putFloat(key: String, value: Float) = writeRaw(key, value.toString())
    override fun getFloat(key: String, defaultValue: Float): Float =
        readRaw(key)?.toFloatOrNull() ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = readRaw(key)?.toFloatOrNull()

    // Double
    override fun putDouble(key: String, value: Double) = writeRaw(key, value.toString())
    override fun getDouble(key: String, defaultValue: Double): Double =
        readRaw(key)?.toDoubleOrNull() ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = readRaw(key)?.toDoubleOrNull()

    // Boolean
    override fun putBoolean(key: String, value: Boolean) = writeRaw(key, value.toString())
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        readRaw(key)?.toBooleanStrictOrNull() ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? =
        readRaw(key)?.toBooleanStrictOrNull()
}

actual fun createSettings(): Settings =
    KeychainSettings(service = "org.smlpartners.smlgo.secure")