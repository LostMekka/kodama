package de.lostmekka.kodama

abstract class KodamaException(message: String) : Exception(message)

class KodamaInitializationException(message: String) : KodamaException(message)
internal fun initError(message: String): Nothing = throw KodamaInitializationException(message)

class KodamaRuntimeException(message: String) : KodamaException(message)
internal fun runtimeError(message: String): Nothing = throw KodamaRuntimeException(message)
