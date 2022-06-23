package de.lostmekka.kodama.ctor

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class ObjectCreator<T : Any>(
    internal val constructorToUse: KFunction<T>,
    internal val ctorParamMapping: Map<String, KParameter>,
    internal val setterMapping: Map<String, KMutableProperty1<T, Any?>>,
) {
    fun create(input: Map<String, Any?>): T {
        val params = ctorParamMapping.entries.associate { (name, param) -> param to input.getValue(name) }
        val obj = constructorToUse.callBy(params)
        for ((name, property) in setterMapping) property.setter.invoke(obj, input.getValue(name))
        return obj
    }

    companion object {
        inline fun <reified T: Any> forClass(
            requiredPropertiesToSet: Map<String, KType>,
        ): ObjectCreator<T>? = forClass(T::class, requiredPropertiesToSet)
        fun <T: Any> forClass(
            classToAnalyze: KClass<T>,
            requiredPropertiesToSet: Map<String, KType>,
        ): ObjectCreator<T>? = analyze(classToAnalyze, requiredPropertiesToSet)
    }
}

private fun logDebugLine(line: String = "") {
    println("[DEBUG] $line")
}

private fun <T : Any> analyze(
    classToAnalyze: KClass<T>,
    requiredPropertiesToSet: Map<String, KType>,
): ObjectCreator<T>? {
    @Suppress("UNCHECKED_CAST")
    val allSettablesByName = classToAnalyze.memberProperties
        .mapNotNull { it as? KMutableProperty1<T, Any?> }
        .associateBy { it.name }
    for ((name, prop) in allSettablesByName) {
        logDebugLine("mutable property $name: ${prop.returnType}")
    }

    val creators = classToAnalyze.constructors.mapNotNull { ctor ->
        val isPrimaryCtor = ctor == classToAnalyze.primaryConstructor
        logDebugLine("${ctor.name} (is primary ctor: $isPrimaryCtor)")
        for (parameter in ctor.parameters) {
            logDebugLine("    parameter ${parameter.name}: ${parameter.type}")
        }
        visitCtor(ctor, allSettablesByName, requiredPropertiesToSet)
    }
    logDebugLine()

    if (creators.isEmpty()) return null
    val creatorWithDefaultCtor = creators.find { it.constructorToUse == classToAnalyze.primaryConstructor }
    if (creatorWithDefaultCtor != null) return creatorWithDefaultCtor
    return creators.maxByOrNull { it.ctorParamMapping.size }
}

private fun <T : Any> visitCtor(
    ctor: KFunction<T>,
    allSettablesByName: Map<String, KMutableProperty1<T, Any?>>,
    requiredPropertiesToSet: Map<String, KType>,
): ObjectCreator<T>? {
    if (ctor.parameters.any { it.name == null }) {
        logDebugLine("ctor is unusable because at least one param has no name")
        return null
    }
    val allParamsByName = ctor.parameters.associateBy { it.name!! }
    val allParamNames = allParamsByName.keys
    val requiredNames = requiredPropertiesToSet.keys

    val usableParametersByName = allParamsByName
        .filter { (name, param) ->
            val type = requiredPropertiesToSet[name]
            when {
                type == null -> false // this param is not covered. (will cause problems later on...)
                param.type.isSupertypeOf(type) -> true // this param can accept the input
                else -> {
                    logDebugLine("cannot use ctor param $name, since types do not match. required: $type, actual: ${param.type}")
                    false
                }
            }
        }
    val usableSettablesByName = allSettablesByName
        .filter { (name, settable) ->
            val type = requiredPropertiesToSet[name]
            when {
                type == null -> false // this settable is not needed
                name in usableParametersByName -> false // don't need a settable if we can use the ctor param
                settable.returnType.isSupertypeOf(type) -> true // this settable can accept the input
                else -> {
                    logDebugLine("cannot use settable property $name since types do not match. required: $type, actual: ${settable.returnType}")
                    false
                }
            }
        }

    val missingNames = requiredNames - usableParametersByName.keys - usableSettablesByName.keys
    if (missingNames.isNotEmpty()) {
        logDebugLine("ctor is unusable, because it cannot handle required values for $missingNames")
        return null
    }
    val unaccountedNames = allParamNames - requiredNames
    if (unaccountedNames.isNotEmpty()) {
        logDebugLine("ctor is unusable, because it following params are not accounted for: $unaccountedNames")
        return null
    }

    logDebugLine("ctor is usable. here is the plan:")
    for ((name, param) in usableParametersByName) {
        logDebugLine("    input $name is supplied to constructor param $param")
    }
    for ((name, settable) in usableSettablesByName) {
        logDebugLine("    input $name is supplied via setter of $settable")
    }

    return ObjectCreator(
        constructorToUse = ctor,
        ctorParamMapping = usableParametersByName,
        setterMapping = usableSettablesByName,
    )
}
