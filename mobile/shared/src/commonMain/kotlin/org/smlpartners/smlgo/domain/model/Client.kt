// domain/model/Client.kt
package org.smlpartners.smlgo.domain.model

data class Client(
    val id             : Int,
    val code           : String?,
    val name           : String?,
    val documentType   : DocumentType?,
    val documentNumber : String?,
    val address        : String?,
    val district       : District?,
    val businessType   : BusinessType?,
    val clientGroup    : ClientGroup?,
    val cellphone      : String?,
    val telephone      : String?,
    val active         : Boolean,
    val latitude       : Double?,
    val longitude      : Double?,
    val observation    : String?,
    val supplier       : Supplier?,
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null
}