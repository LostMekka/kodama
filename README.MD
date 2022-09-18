# Kodama - A Kotlin Data Mapper
Kodama is a library and compiler plugin that strives to provide convenient data class mapping while minimizing boilerplate code.

## State of the project
This project is just starting out and I work on it in the little spare time I have. This is the current plan for development:
- [x] Create a compiler plugin that can at least partially validate the Kodama DSL
- [ ] Have a working prototype using runtime validation and reflection
  - [x] mapping (forward + backward)
  - [ ] updating (forward + backward)
  - [ ] collection handling for update operations
- [ ] Use it in a project and see how it feels
- [ ] move all of the validation to the compiler plugin
- [ ] Let the compiler plugin generate everything, so runtime scanning and reflection is no longer needed
- [ ] Profit?

## What is a Kodama?
Kodama stands for Kotlin Data Mapper. A single Kodama is a mapper object that can map objects from a source type to a target type. The Kodama DSL lets you build custom Kodamas, while the compiler plugin makes sure that the configured Kodamas fit together nicely and will behave correctly at runtime.

Kodama is also [type of Japanese forest spirit](https://en.wikipedia.org/wiki/Kodama_(spirit)), perhaps most famously featured in films from [Studio Ghibli](https://ghibli.fandom.com/wiki/Kodama).

Just as the "real" Kodamas, the Kodamas built with this library live in trees: Nested data structures can be mapped by nested Kodamas.

## Why do I need Kodama?
### The problem
I personally often encounter this use case:
- A backend application has a REST API and a database
- The REST API is powered by OpenAPI generated code, so request/response payload data classes and REST controller interfaces are generated and consumed by the application as a maven dependency
- The database part uses JPA entity classes, which use constructor properties to ensure that the app code cannot accidentally create half-filled entities
- There are CRUD operations that do the following:
  - Take the request payload
  - Update the database
  - Return a response payload with the modified data

In this case, there are three different mapper functions that need to be implemented:
- Create a new entity from a request payload object
- Update an existing entity from a request payload object
- Create a new response payload object

Let's look at how this is how this would be written traditionally. We assume these are our data classes:
```kotlin
 // generated from OpenAPI
data class UserApiPayload(
  val id: UUID?,
  val name: String,
  val address: String,
)

// our entity. just imagine the JPA annotations here; they are not relevant to the example
class UserEntity(
  val id: UUID = UUID.randomUUID(),
  var name: String,
  var address: String,
)
```

Then our three mappers would be:
```kotlin
fun UserApiPayload.toEntity() =
  UserEntity(
    name = name,
    address = address,
  )
fun UserEntity.updateFrom(input: UserApiPayload) {
    name = input.name
    address = input.address
}
fun UserEntity.toResponsePayload() =
  UserApiPayload(
    id = id,
    name = name,
    address = address,
  )
```

This is a very small example, but there is already significant duplication of logic present. The whole data mapping logic is spread or duplicated over several functions, which is not very good for code readability, comprehension and maintainability. 

Let's say we introduce a new property to our user. We implement all the changes, but forget to also add a line in `updateFrom`. This introduces a bug, where an update request just does not update this new field. As entities gain more fields, this error can be hard to detect before shipping your app to its users.

When these data classes get larger, there is also the problem that copy/paste errors can happen. For example, one of the three mappers may accidentally switch the firstName and lastName fields. Spotting these issues in code review can become very tedious.

Unit tests for those mappers aren't really helping here either, because then you just have one more function that contains the mapping logic, which has to be maintained.

### The solution
There should be no need to write the property mapping more than once, since it is tedious enough when done once. Kodama to the rescue!
```kotlin
// note that the final DSL may be a bit different
val userMapper = kodama {
  capabilities(mapForward, updateForward, mapBackward)
  UserApiPayload::id mapsTo UserEntity::id { backwardOnly }
  UserApiPayload::name mapsTo UserEntity::name
  UserApiPayload::address mapsTo UserEntity::address
}

val input = UserApiPayload(...)
// create a new entity
val entity = userMapper.mapForward(input)
// or update an existing entity
val entity = userMapper.updateForward(input, entity)
// and map back to the api type
val output = userMapper.mapBackward(entity)
```

## Types of Kodamas
There are many types of Kodamas to play with:

#### The PrimitiveKodama
This Kodama is used to map fairly primitive types by hand, like UUIDs to Strings:
```kotlin
val uuidToStringKodama = primitiveKodama<UUID, String>(
    forward = { it.toString() },
    backward = { UUID.fromString(it) },
)
```
Note, that primitive Kodamas can just map and cannot update. They are only used for leaf nodes in you data structure tree. Whenever a primitive Kodama is used in an update operation, it will be instructed to use its map operation.

#### The IdentityKodama
This is a special variant of the PrimitiveKodama, and it is the laziest of the bunch. it does nothing, since the input and output types are equal.

#### The EnumKodama
This Kodama maps enum values:
```kotlin
val userRoleKodama = enumKodama<ApiRole, DbRole> {
    ApiRole.User mapsTo DbRole.User
    ApiRole.Mod mapsTo DbRole.Moderator
    ApiRole.Admin mapsTo DbRole.Administrator
}
```
Just like the PrimitiveKodama, it can only map.

#### Collection Kodamas
These Kodamas are there to map Lists, Sets and the like. They use a nested Kodama to perform the operations on the elements in the collection.

#### The NullableKodama
This Kodama just handles nullability. Like the collection Kodamas, it delegates the real work to a nested Kodama.

#### The ObjectKodama
This big boy is the heart of this library. It is constructed via the Kodama DSL and can map or update forwards or backwards, depending on its configured capabilities. Primitive fields that are the same in the input and the output are mapped directly. For all other fields, the ObjectKodama delegates the operations to its nested Kodamas.

## Kodama operations
Depending on their capabilities, Kodamas can perform one of those operations.

#### Mapping
Mapping means taking an instance of one type and creating a new instance of the other type from it. Primitive values are copied and more complex inner objects are mapped by nested Kodamas.

Mapping can be done in two directions: MapForward maps A -> B, while MapBackward maps B -> A. The target type needs to have an appropriate combination of constructor and/or mutable properties, so every property can be initialized properly.

#### Updating
Updating means taking an instance of one type and using its contents to fill an existing instance the other type.

Updating can also be done in both directions. The target type needs to have mutable properties for every field that needs to be mapped.

#### Cloning???
This may be a thing that the IdentityKodama can do. This would result in deep clones, like mapping to the same type. But it is just an idea for now.
