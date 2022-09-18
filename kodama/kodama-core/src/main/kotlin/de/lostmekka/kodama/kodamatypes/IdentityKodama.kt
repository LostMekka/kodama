package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import kotlin.reflect.KType

internal class IdentityKodama<T>(
    valueType: KType,
) : Kodama<T, T> {
    override fun mapForward(source: T): T = source
    override fun mapBackward(source: T): T = source
    override fun updateForward(source: T, target: T): T = source
    override fun updateBackward(source: T, target: T): T = source
    override val canMapBackward = true
    override val canUpdateForward = false
    override val canUpdateBackward = false

    override fun reversed(): Kodama<T, T> = this
    override fun reversedOrNull(): Kodama<T, T> = this

    override val sourceType: KType = valueType
    override val targetType: KType = valueType
}
