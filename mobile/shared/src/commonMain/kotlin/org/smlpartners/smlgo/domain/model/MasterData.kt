// domain/model/MasterData.kt
package org.smlpartners.smlgo.domain.model

// Agrupamos los catálogos en un solo archivo para no fragmentar

data class DocumentType(
    val id         : Int,
    val description: String
)

data class BusinessType(
    val id         : Int,
    val description: String
)

data class ClientGroup(
    val id         : Int,
    val description: String
)

data class Supplier(
    val id    : Int,
    val code  : String,
    val names : String,
    val active: Boolean
)