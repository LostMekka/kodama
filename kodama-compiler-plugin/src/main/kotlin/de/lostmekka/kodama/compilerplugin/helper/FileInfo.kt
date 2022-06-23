package de.lostmekka.kodama.compilerplugin.helper

import java.io.File
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocationWithRange
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path

class FileInfo(val file: IrFile) {
    val fileSource by lazy { File(file.path).readText() }
    fun createMessageLocation(element: IrElement): CompilerMessageLocationWithRange {
        val info = file.fileEntry.getSourceRangeInfo(element.startOffset, element.endOffset)
        return CompilerMessageLocationWithRange.create(
            path = info.filePath,
            lineStart = info.startLineNumber + 1,
            columnStart = info.startColumnNumber + 1,
            lineEnd = info.endLineNumber + 1,
            columnEnd = info.endColumnNumber + 1,
            lineContent = fileSource.substring(element.startOffset, element.endOffset),
        )!!
    }
}
