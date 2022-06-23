package de.lostmekka.kodama.samples

import de.lostmekka.kodama.kodamatypes.enumKodama

enum class ApiRole { User, Mod, Admin }
enum class DbRole { User, Moderator, Administrator }
val roleKodama =
    enumKodama<ApiRole, DbRole> {
        ApiRole.User mapsTo DbRole.User
        ApiRole.Mod mapsTo DbRole.Moderator
        ApiRole.Admin mapsTo DbRole.Administrator
    }

fun main() {
    ApiRole.values().forEach { println("ApiRole.$it -> DbRole.${roleKodama.mapForward(it)}") }
    DbRole.values().forEach { println("DbRole.$it -> ApiRole.${roleKodama.mapBackward(it)}") }
}
