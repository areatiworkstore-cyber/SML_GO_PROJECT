// domain/model/User.kt
package org.smlpartners.smlgo.domain.model

data class User(
    val id: Int,
    val code: String,
    val firstName: String,
    val secondName: String,
    val firstSurname: String,
    val secondSurname: String,
    val documentType: DocumentType?,
    val documentNumber: String,
    val cellphone: String?,
    val email: String,
    val roles: List<Role>,
    val active: Boolean
)

data class Profile (
    val id           : Int,
    val code         : String,
    val firstName    : String,
    val secondName   : String,
    val firstSurname : String,
    val secondSurname: String,
    val documentType: DocumentType?,
    val documentNumber: String,
    val cellphone    : String,
    val email        : String,
    val roles        : List<Role>
)