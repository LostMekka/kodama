package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import de.lostmekka.kodama.KodamaDsl
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability

internal class ListKodama<A, B>(
    private val innerKodama: Kodama<A, B>,
) : Kodama<List<A>, List<B>> {
    override fun mapForward(source: List<A>): List<B> = source.map { innerKodama.mapForward(it) }
    override fun mapBackward(source: List<B>): List<A> = source.map { innerKodama.mapBackward(it) }
    override fun updateForward(source: List<A>, target: List<B>): List<B> = TODO("not implemented")
    override fun updateBackward(source: List<B>, target: List<A>): List<A> = TODO("not implemented")
    override val canMapBackward by lazy { innerKodama.canMapBackward }
    override val canUpdateForward by lazy { false }
    override val canUpdateBackward by lazy { false }

    override val sourceType: KType = List::class.createType(listOf(KTypeProjection(KVariance.OUT, innerKodama.sourceType)))
    override val targetType: KType = List::class.createType(listOf(KTypeProjection(KVariance.OUT, innerKodama.targetType)))
}

fun <A, B> Kodama<A, B>.asListKodama(): Kodama<List<A>, List<B>> = ListKodama(this)
