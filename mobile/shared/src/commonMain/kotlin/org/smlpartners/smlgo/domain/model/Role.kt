// domain/model/Role.kt
package org.smlpartners.smlgo.domain.model

data class Role(
    val id  : Int = 0,  // 0 cuando viene de /users/me (solo nombre, sin id)
    val role: String
)