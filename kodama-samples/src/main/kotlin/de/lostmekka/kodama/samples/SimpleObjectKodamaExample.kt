package de.lostmekka.kodama.samples

import de.lostmekka.kodama.kodama
import de.lostmekka.kodama.kodamatypes.primitiveKodama
import java.util.UUID

data class SimpleUserDto(val id: String, val name: String)
data class SimpleUserEntity(val id: UUID, val name: String)

private val uuidToStringKodama = primitiveKodama<UUID, String>(
    forward = { it.toString() },
    backward = { UUID.fromString(it) },
)
private val simpleKodama = kodama<SimpleUserDto, SimpleUserEntity> {
    mapping(SimpleUserDto::id, SimpleUserEntity::id, uuidToStringKodama.reversed())
    // or specify the mapping directly, if you do not want to reuse it as a kodama:
    // mapping(SimpleUserDto::id, SimpleUserEntity::id, mapping(forward = { UUID.fromString(it) }, backward = { it.toString() }))
    mapping(SimpleUserDto::name, SimpleUserEntity::name)
}

fun main() {
    val dto = SimpleUserDto(UUID.randomUUID().toString(), "example")
    println("initial DTO:       $dto")
    val entity = simpleKodama.mapForward(dto)
    println("mapped entity:     $entity")
    val dtoAgain = simpleKodama.mapBackward(entity)
    println("back to DTO again: $dtoAgain")
}
