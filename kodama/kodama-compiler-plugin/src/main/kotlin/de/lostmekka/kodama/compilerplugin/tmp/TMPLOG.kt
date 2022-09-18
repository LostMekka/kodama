package de.lostmekka.kodama.compilerplugin.tmp

import java.io.File
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.util.dump

class TMPLOG(filePath: String) {
    private val tmpFile = File(filePath).also { it.writeText("") }

    fun dump(element: IrElement?, tag: String? = null) {
        if (tag == null) {
            tmpFile.appendText("================================================================\n")
        } else {
            tmpFile.appendText("===== $tag =======================================================\n")
        }
        tmpFile.appendText("${element?.dump()}\n")
    }

    fun log(data: Any?, tag: String? = null) {
        if (tag == null) {
            tmpFile.appendText("===== LOG ==============================================================\n")
        } else {
            tmpFile.appendText("===== LOG === $tag =======================================================\n")
        }
        tmpFile.appendText("$data\n\n")
    }
}
