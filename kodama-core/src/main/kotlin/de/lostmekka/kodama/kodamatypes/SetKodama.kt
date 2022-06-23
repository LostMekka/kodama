package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import de.lostmekka.kodama.KodamaDsl
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability

internal class SetKodama<A, B>(
    private val innerKodama: Kodama<A, B>,
) : Kodama<Set<A>, Set<B>> {
    override fun mapForward(source: Set<A>): Set<B> = source.mapTo(mutableSetOf()) { innerKodama.mapForward(it) }
    override fun mapBackward(source: Set<B>): Set<A> = source.mapTo(mutableSetOf()) { innerKodama.mapBackward(it) }
    override fun updateForward(source: Set<A>, target: Set<B>): Set<B> = TODO("not implemented")
    override fun updateBackward(source: Set<B>, target: Set<A>): Set<A> = TODO("not implemented")
    override val canMapBackward by lazy { innerKodama.canMapBackward }
    override val canUpdateForward by lazy { false }
    override val canUpdateBackward by lazy { false }

    override val sourceType: KType = Set::class.createType(listOf(KTypeProjection(KVariance.OUT, innerKodama.sourceType)))
    override val targetType: KType = Set::class.createType(listOf(KTypeProjection(KVariance.OUT, innerKodama.targetType)))
}

fun <A, B> Kodama<A, B>.asSetKodama(): Kodama<Set<A>, Set<B>> = SetKodama(this)
