// domain/model/Geography.kt
package org.smlpartners.smlgo.domain.model

// Jerarquía geográfica
data class Department(
    val id      : Int,
    val name    : String,
    val active  : Boolean,
    val provinces: List<Province> = emptyList()
)

data class Province(
    val id           : Int,
    val name         : String,
    val active       : Boolean,
    val departmentId : Int,
    val districts    : List<District> = emptyList()
)

data class District(
    val id        : Int,
    val name      : String,
    val active    : Boolean,
    val provinceId: Int
)