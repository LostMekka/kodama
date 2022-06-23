package de.lostmekka.kodama.samples

import de.lostmekka.kodama.ctor.ObjectCreator
import kotlin.reflect.full.starProjectedType

@Suppress("unused", "UNUSED_PARAMETER")
data class Test(val id: Int, val name: String) {
    constructor(id: Int) : this(id, "default")
    constructor(id: Int, name: String, somethingElse: Double) : this(id, name)
    var test: String = "initial value"
    lateinit var lateInitTest: String
}

fun main() {
    val requiredPropertiesToSet = mapOf(
        "id" to Int::class.starProjectedType,
        "name" to String::class.starProjectedType,
        "test" to String::class.starProjectedType,
        "lateInitTest" to String::class.starProjectedType,
    )
    val creator = ObjectCreator
        .forClass<Test>(requiredPropertiesToSet)
        ?: error("no creator found!")
    val obj = creator.create(
        mapOf(
            "id" to 1,
            "name" to "nameValue",
            "test" to "testValue",
            "lateInitTest" to "lateInitTestValue",
        )
    )
    println(obj.id)
    println(obj.name)
    println(obj.test)
    println(obj.lateInitTest)
}
