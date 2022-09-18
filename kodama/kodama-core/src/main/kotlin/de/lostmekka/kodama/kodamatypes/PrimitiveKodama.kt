package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import de.lostmekka.kodama.runtimeError
import kotlin.reflect.KType
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
import kotlin.reflect.jvm.reflect

@OptIn(ExperimentalReflectionOnLambdas::class)
internal class PrimitiveKodama<A, B>(
    private val forward: (A) -> B,
    private val backward: ((B) -> A)? = null,
) : Kodama<A, B> {
    override fun mapForward(source: A): B = forward(source)
    override fun mapBackward(source: B): A {
        val mapper = backward ?: runtimeError("This primitive kodama is not configured for backward mapping.")
        return mapper(source)
    }
    override fun updateForward(source: A, target: B): B = runtimeError("Primitive kodamas cannot update.")
    override fun updateBackward(source: B, target: A): A = runtimeError("Primitive kodamas cannot update.")
    override val canMapBackward = backward != null
    override val canUpdateForward = false
    override val canUpdateBackward = false
    override val sourceType: KType = forward.reflect()!!.parameters.first().type
    override val targetType: KType = forward.reflect()!!.returnType
}

fun <A, B> primitiveKodama(
    forward: (A) -> B,
    backward: ((B) -> A)? = null,
): Kodama<A, B> = PrimitiveKodama(forward, backward)
