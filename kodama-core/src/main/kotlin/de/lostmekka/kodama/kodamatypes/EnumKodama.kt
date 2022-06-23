package de.lostmekka.kodama.kodamatypes

import de.lostmekka.kodama.Kodama
import de.lostmekka.kodama.KodamaDsl
import de.lostmekka.kodama.initError
import de.lostmekka.kodama.runtimeError
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

internal class EnumKodama<A: Enum<A>, B: Enum<B>>(
    aClass: KClass<A>,
    bClass: KClass<B>,
    pairs: List<Pair<A, B>>,
    backwardMappingRequired: Boolean,
) : Kodama<A, B> {
    private val forwardMap: Map<A, B>
    private val backwardMap: Map<B, A>?
    override val canMapBackward: Boolean
    override val canUpdateForward: Boolean = false
    override val canUpdateBackward: Boolean = false
    init {
        forwardMap = validate(pairs, aClass, bClass, isForward = true)
        backwardMap = when (backwardMappingRequired) {
            true -> validate(pairs.swapped(), bClass, aClass, isForward = false)
            else -> null
        }
        canMapBackward = backwardMap != null
    }

    override fun mapForward(source: A): B =
        forwardMap.getValue(source)

    override fun mapBackward(source: B): A =
        backwardMap?.getValue(source)
            ?: runtimeError("cannot backward map enum value $source: value has no configured counterpart")

    override fun updateForward(source: A, target: B): B = runtimeError("enum kodamas cannot update")
    override fun updateBackward(source: B, target: A): A = runtimeError("enum kodamas cannot update")

    override val sourceType: KType = aClass.starProjectedType
    override val targetType: KType = bClass.starProjectedType
}

private fun <A, B> List<Pair<A, B>>.swapped() = map { (a, b) -> b to a }

private fun <A: Enum<A>, B: Enum<B>> validate(
    pairs: List<Pair<A, B>>,
    sourceClass: KClass<A>,
    targetClass: KClass<B>,
    isForward: Boolean,
): Map<A, B> {
    val direction = if (isForward) "forward" else "backward"
    val valueCounts = pairs.groupingBy { it.first }.eachCount().withDefault { 0 }
    val errors = sourceClass.java.enumConstants
        .mapNotNull {
            when (valueCounts.getValue(it)) {
                0 -> "No $direction mapping for value $it configured"
                1 -> null
                else -> "Multiple $direction mappings for value $it configured"
            }
        }
    if (errors.isNotEmpty()) initError(
        "enum kodama cannot create $direction mapping $sourceClass -> $targetClass\n${errors.size} errors found:" +
            errors.joinToString("") { "\n    $it" }
    )
    return pairs.associate { it }
}

inline fun <reified A: Enum<A>, reified B: Enum<B>> enumKodama(
    noinline config: EnumKodamaBuilder<A, B>.() -> Unit,
) = enumKodama(A::class, B::class, config)

fun <A: Enum<A>, B: Enum<B>> enumKodama(
    sourceClass: KClass<A>,
    targetClass: KClass<B>,
    config: EnumKodamaBuilder<A, B>.() -> Unit,
): Kodama<A, B> =
    EnumKodamaBuilder(
        sourceClass,
        targetClass,
    ).apply(config).build()

@KodamaDsl
class EnumKodamaBuilder<A: Enum<A>, B: Enum<B>> internal constructor(
    private val sourceClass: KClass<A>,
    private val targetClass: KClass<B>,
) {
    private val pairs = mutableListOf<Pair<A, B>>()
    private var mapBackwards = true
    fun forwardOnly() {
        mapBackwards = false
    }
    infix fun A.mapsTo(target: B) {
        pairs += this to target
    }
    internal fun build() = EnumKodama(sourceClass, targetClass, pairs, mapBackwards)
}
