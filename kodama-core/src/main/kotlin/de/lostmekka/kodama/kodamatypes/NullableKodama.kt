package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

internal class NullableKodama<A: Any, B: Any>(
    private val innerKodama: Kodama<A, B>,
) : Kodama<A?, B?> {
    override fun mapForward(source: A?): B? = source?.let { innerKodama.mapForward(it) }
    override fun mapBackward(source: B?): A? = source?.let { innerKodama.mapBackward(it) }
    override fun updateForward(source: A?, target: B?): B? =
        when {
            source == null -> null
            target == null -> innerKodama.mapForward(source)
            else -> innerKodama.updateForward(source, target)
        }
    override fun updateBackward(source: B?, target: A?): A? =
        when {
            source == null -> null
            target == null -> innerKodama.mapBackward(source)
            else -> innerKodama.updateBackward(source, target)
        }
    override val canMapBackward by lazy { innerKodama.canMapBackward }
    override val canUpdateForward by lazy { innerKodama.canUpdateForward }
    override val canUpdateBackward by lazy { innerKodama.canUpdateBackward }

    override val sourceType: KType = innerKodama.sourceType.withNullability(true)
    override val targetType: KType = innerKodama.targetType.withNullability(true)
}
