package de.lostmekka.kodama.samples

import de.lostmekka.kodama.kodama
import de.lostmekka.kodama.kodamatypes.asListKodama
import de.lostmekka.kodama.kodamatypes.primitiveKodama
import java.util.UUID

data class UserDto(val id: String, val name: String, val addresses: List<AddressDto>)
data class AddressDto(val street: String, val city: String)

data class UserEntity(val id: UUID, val name: String, val addresses: List<AddressEntity>)
data class AddressEntity(val street: String, val city: String)

private val stringToUuidKodama = primitiveKodama<String, UUID>(
    forward = { UUID.fromString(it) },
    backward = { it.toString() },
)
private val addressKodama = kodama<AddressDto, AddressEntity> {
    mapping(AddressDto::street, AddressEntity::street)
    mapping(AddressDto::street, AddressEntity::city)
}
private val nestedKodama = kodama<UserDto, UserEntity> {
    mapping(UserDto::id, UserEntity::id, stringToUuidKodama)
    mapping(UserDto::name, UserEntity::name)
    mapping(UserDto::addresses, UserEntity::addresses, addressKodama.asListKodama())
}

fun main() {
    val dto = UserDto(
        id = UUID.randomUUID().toString(),
        name = "example",
        addresses = listOf(
            AddressDto("street1", "city1"),
            AddressDto("street2", "city2"),
        ),
    )
    println("initial DTO:       $dto")
    val entity = nestedKodama.mapForward(dto)
    println("mapped entity:     $entity")
    val dtoAgain = nestedKodama.mapBackward(entity)
    println("back to DTO again: $dtoAgain")
}
