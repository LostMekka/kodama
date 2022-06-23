package de.lostmekka.kodama

import kotlin.reflect.KType

@DslMarker
annotation class KodamaDsl

interface Kodama<A, B> {
    fun mapForward(source: A): B
    fun mapBackward(source: B): A
    fun updateForward(source: A, target: B): B
    fun updateBackward(source: B, target: A): A
    val canMapBackward: Boolean
    val canUpdateForward: Boolean
    val canUpdateBackward: Boolean
    val sourceType: KType
    val targetType: KType

    fun reversed(): Kodama<B, A> = reversedOrNull() ?: runtimeError("this kodama cannot be reversed")
    fun reversedOrNull(): Kodama<B, A>? {
        if (!canMapBackward) return null
        return object : Kodama<B, A> {
            override fun mapForward(source: B): A = this@Kodama.mapBackward(source)
            override fun mapBackward(source: A): B = this@Kodama.mapForward(source)
            override fun updateForward(source: B, target: A): A = this@Kodama.updateBackward(source, target)
            override fun updateBackward(source: A, target: B): B = this@Kodama.updateForward(source, target)
            override val canMapBackward = true
            override val canUpdateForward = this@Kodama.canUpdateBackward
            override val canUpdateBackward = this@Kodama.canUpdateForward
            override val sourceType: KType = this@Kodama.targetType
            override val targetType: KType = this@Kodama.sourceType
            override fun reversed(): Kodama<A, B> = this@Kodama
            override fun reversedOrNull(): Kodama<A, B> = this@Kodama
        }
    }
}
