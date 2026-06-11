// domain/model/Client.kt
package org.smlpartners.smlgo.domain.model

data class Client(
    val id             : Int = 0,
    val code           : String? = null,
    val name           : String? = null,
    val documentType   : DocumentType? = null,
    val documentNumber : String? = null,
    val address        : String? = null,
    val district       : District? = null,
    val businessType   : BusinessType? = null,
    val clientGroup    : ClientGroup? = null,
    val cellphone      : String? = null,
    val telephone      : String? = null,
    val active         : Boolean = true,
    val latitude       : Double? = null,
    val longitude      : Double? = null,
    val observation    : String? = null,
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null
}

data class NextCode(
    val nextCode: String? = null
)