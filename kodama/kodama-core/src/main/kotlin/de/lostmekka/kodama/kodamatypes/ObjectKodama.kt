package de.lostmekka.kodama

import de.lostmekka.kodama.ctor.ObjectCreator
import de.lostmekka.kodama.kodamatypes.IdentityKodama
import de.lostmekka.kodama.kodamatypes.PrimitiveKodama
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

internal class ObjectKodama<A : Any, B : Any>(
    override val sourceType: KType,
    override val targetType: KType,
    private val propertyMappers: List<PropertyMapper<Any?, Any?>>,
    private val forwardObjectCreator: ObjectCreator<B>,
    private val backwardObjectCreator: ObjectCreator<A>?,
) : Kodama<A, B> {
    override fun mapForward(source: A): B {
        val data =
            propertyMappers.associate { it.targetProperty.name to it.innerKodama.mapForward(it.sourceProperty.get(source)) }
        return forwardObjectCreator.create(data)
    }

    override fun mapBackward(source: B): A {
        if (!canMapBackward) runtimeError("this kodama cannot map backwards")
        val data = propertyMappers.associate {
            it.sourceProperty.name to it.innerKodama.mapBackward(it.targetProperty.get(source))
        }
        return backwardObjectCreator!!.create(data)
    }

    override fun updateForward(source: A, target: B): B {
        TODO("not implemented")
    }

    override fun updateBackward(source: B, target: A): A {
        TODO("not implemented")
    }

    override val canMapBackward by lazy { backwardObjectCreator != null && propertyMappers.all { it.innerKodama.canMapBackward } }
    override val canUpdateForward by lazy { propertyMappers.all { it.innerKodama.canUpdateForward } } // TODO: this is not true!
    override val canUpdateBackward by lazy { propertyMappers.all { it.innerKodama.canUpdateBackward } } // TODO: this is not true!
}

internal class PropertyMapper<A, B>(
    val sourceProperty: KProperty1<Any, A>,
    val targetProperty: KProperty1<Any, B>,
    val innerKodama: Kodama<A, B>,
)

class CustomPropertyMapping<A, B> internal constructor(
    internal val forward: (A) -> B,
    internal val backward: (B) -> A,
)

fun <A, B> mapping(
    forward: (A) -> B,
    backward: (B) -> A,
) = CustomPropertyMapping(forward, backward)

@KodamaDsl
class ObjectKodamaBuilder<A : Any, B : Any> internal constructor(
    private val forwardClass: KClass<B>,
    private val backwardClass: KClass<A>,
) {
    // TODO: check for duplicate references!
    private val propertyMappers = mutableListOf<PropertyMapper<Any?, Any?>>()

    // TODO: find a way to prevent V from just going up to Any, if the property types differ
    fun <@kotlin.internal.OnlyInputTypes V> mapping(
        source: KProperty1<A, V>,
        target: KProperty1<B, V>,
    ) {
        if (source.returnType != target.returnType) initError("cannot map $source and $target directly since they have different types")
        mapping(source, target, IdentityKodama(source.returnType))
    }

    fun <VA, VB> mapping(
        source: KProperty1<A, VA>,
        target: KProperty1<B, VB>,
        mapping: (VA) -> VB,
    ) {
        mapping(source, target, PrimitiveKodama(forward = mapping))
    }

    fun <VA, VB> mapping(
        source: KProperty1<A, VA>,
        target: KProperty1<B, VB>,
        forwardsMapping: (VA) -> VB,
        backwardsMapping: (VB) -> VA,
    ) {
        mapping(source, target, PrimitiveKodama(forward = forwardsMapping, backward = backwardsMapping))
    }

    fun <VA, VB> mapping(
        source: KProperty1<A, VA>,
        target: KProperty1<B, VB>,
        mapping: Kodama<VA, VB>,
    ) {
        propertyMappers += PropertyMapper(
            source as KProperty1<Any, A>,
            target as KProperty1<Any, B>,
            mapping as Kodama<A, B>,
        ) as PropertyMapper<Any?, Any?>
    }

    internal fun build(): Kodama<A, B> {
        return ObjectKodama(
            sourceType = backwardClass.starProjectedType,
            targetType = forwardClass.starProjectedType,
            propertyMappers = propertyMappers,
            forwardObjectCreator = ObjectCreator
                .forClass(
                    forwardClass,
                    propertyMappers.associate {
                        it.targetProperty.name to it.innerKodama.targetType
                    },
                )
                ?: initError("cannot create kodama: there is no way to create instances of $forwardClass"),
            backwardObjectCreator = ObjectCreator
                .forClass(
                    backwardClass,
                    propertyMappers.associate {
                        it.sourceProperty.name to it.innerKodama.sourceType
                    },
                ),
        )
    }
}

inline fun <reified A : Any, reified B : Any> kodama(
    noinline config: ObjectKodamaBuilder<A, B>.() -> Unit,
): Kodama<A, B> =
    kodama(B::class, A::class, config)

fun <A : Any, B : Any> kodama(
    forwardClass: KClass<B>,
    backwardClass: KClass<A>,
    config: ObjectKodamaBuilder<A, B>.() -> Unit,
): Kodama<A, B> =
    ObjectKodamaBuilder(forwardClass, backwardClass)
        .apply(config)
        .build()
