// domain/model/Supplier.kt
package org.smlpartners.smlgo.domain.model

data class Supplier(
    val id    : Int,
    val code  : String,
    val names : String,
    val active: Boolean
)