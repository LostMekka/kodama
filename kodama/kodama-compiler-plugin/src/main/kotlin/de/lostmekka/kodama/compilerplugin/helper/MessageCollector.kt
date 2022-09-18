package de.lostmekka.kodama.compilerplugin.helper

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement

fun MessageCollector.error(message: String, fileInfo: FileInfo, location: IrElement) {
    report(
        CompilerMessageSeverity.ERROR,
        message,
        fileInfo.createMessageLocation(location),
    )
}
