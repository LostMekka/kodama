package de.lostmekka.kodama.compilerplugin.helper

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

fun IrExpression.constStringValueOrNull() = (this as? IrConst<String>)?.value

fun IrCall.findParameterOrNull(name: String) =
    getArgumentsWithIr().find { it.first.name.asString() == name }?.second

fun IrElement.findCallsTo(function: IrSimpleFunctionSymbol): List<IrCall> =
    mutableListOf<IrCall>().also { accept(CallCollectorVisitor(function), it) }

private class CallCollectorVisitor(
    private val function: IrSimpleFunctionSymbol,
) : IrElementVisitor<Unit, MutableList<IrCall>> {
    override fun visitCall(expression: IrCall, data: MutableList<IrCall>) {
        if (expression.symbol == function) {
            data.add(expression)
        } else {
            super.visitCall(expression, data)
        }
    }

    override fun visitElement(element: IrElement, data: MutableList<IrCall>) {
        element.acceptChildren(this, data)
    }
}
