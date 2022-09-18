package de.lostmekka.kodama.compilerplugin

import de.lostmekka.kodama.compilerplugin.helper.FileInfo
import de.lostmekka.kodama.compilerplugin.helper.error
import de.lostmekka.kodama.compilerplugin.helper.findCallsTo
import de.lostmekka.kodama.compilerplugin.helper.findParameterOrNull
import de.lostmekka.kodama.compilerplugin.tmp.TMPLOG
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParametersCount
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.name.FqName

private val log = TMPLOG("D:\\WIN\\dev\\kotlin\\kodama\\dump.txt")

internal class KodamaIrGenerationExtension(
    private val messageCollector: MessageCollector,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        for (file in moduleFragment.files) {
            file.transform(KodamaMainVisitor(pluginContext, messageCollector, FileInfo(file)), null)
        }
    }
}

class KodamaMainVisitor(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val file: FileInfo,
) : IrElementTransformerVoidWithContext() {
    private val dslBody = pluginContext
        .referenceClass(FqName("de.lostmekka.kodama.ObjectKodamaBuilder"))!!

    private val mappingFunctions = pluginContext
        .referenceFunctions(FqName("de.lostmekka.kodama.ObjectKodamaBuilder.mapping"))

    private val directMappingFunction = mappingFunctions.single { it.owner.allParametersCount == 3 }

    private val dslFun = pluginContext
        .referenceFunctions(FqName("de.lostmekka.kodama.kodama"))
        .single { it.owner.allParametersCount == 1 }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol != dslFun) return super.visitCall(expression)

        // TODO: find lambda param safely instead of just by name
        val (_, dslBlock) = expression.getArgumentsWithIr().single { it.first.name.asString() == "config" }
        val mappings = dslBlock
            .findCallsTo(directMappingFunction)
            .mapNotNull { call ->
                val sourceParam = call.findParameterOrNull("source")!!.let {
                    if (it is IrPropertyReference) {
                        it
                    } else {
                        messageCollector.error("only property references are allowed here", file, it)
                        null
                    }
                }
                val targetParam = call.findParameterOrNull("target")!!.let {
                    if (it is IrPropertyReference) {
                        it
                    } else {
                        messageCollector.error("only property references are allowed here", file, it)
                        null
                    }
                }
                if (sourceParam == null || targetParam == null) null else Mapping(call, sourceParam, targetParam)
            }
        mappings.filter { it.sourceType != it.targetType }
            .forEach { messageCollector.error("type mismatch", file, it.mappingFunctionCall) }
        mappings.groupBy { it.sourcePropertyName }
            .filterValues { it.size > 1 }
            .forEach { (propertyName, duplicates) ->
                for (duplicate in duplicates) {
                    messageCollector.error("duplicate mapping for source property '$propertyName'", file, duplicate.mappingFunctionCall)
                }
            }
        mappings.groupBy { it.targetPropertyName }
            .filterValues { it.size > 1 }
            .forEach { (propertyName, duplicates) ->
                for (duplicate in duplicates) {
                    messageCollector.error("duplicate mapping for target property '$propertyName'", file, duplicate.mappingFunctionCall)
                }
            }

        return super.visitCall(expression)
    }
}

private class Mapping(
    val mappingFunctionCall: IrCall,
    val sourcePropertyParam: IrPropertyReference,
    val targetPropertyParam: IrPropertyReference,
) {
    val sourcePropertyName = sourcePropertyParam.referencedName.identifier
    val targetPropertyName = targetPropertyParam.referencedName.identifier
    val sourceType = sourcePropertyParam.getter!!.owner.returnType
    val targetType = targetPropertyParam.getter!!.owner.returnType
}
