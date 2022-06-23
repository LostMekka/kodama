package de.lostmekka.kodama.compilerplugin.helper

fun <T> T.ifNull(block: () -> Unit): T {
    if (this == null) block()
    return this
}
