package com.example.calculadora

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val OPERATOR_CHARS = "+-×÷^"

/**
 * Holds the calculator's input state and turns button presses into a valid expression.
 *
 * The raw [expression] string is the single source of truth. It only ever contains digits,
 * '.', the operator symbols in [OPERATOR_CHARS] and, occasionally, a single "(-number)" group
 * produced when a scientific function yields a negative result mid-expression.
 */
class CalculatorState {

    var expression by mutableStateOf("")
        private set

    var isError by mutableStateOf(false)
        private set

    var justEvaluated by mutableStateOf(false)
        private set

    private var lastEvaluatedExpression by mutableStateOf("")

    /** Large, primary readout: the number currently being typed, or the last result. */
    val bigDisplay: String
        get() = when {
            isError -> "Erro"
            expression.isEmpty() -> "0"
            else -> currentOperand(expression).ifEmpty { "0" }
        }

    /** Small, secondary readout: the full expression as typed, or "expr =" after evaluating. */
    val smallDisplay: String
        get() = when {
            isError || justEvaluated -> "$lastEvaluatedExpression ="
            else -> expression
        }

    fun onDigit(digit: String) {
        resetIfNeeded()
        expression += digit
    }

    fun onDecimal() {
        resetIfNeeded()
        val operand = currentOperand(expression)
        if (operand.contains('.') || operand.endsWith(')')) return // lock: duplicate decimal point
        expression += if (operand.isEmpty()) "0." else "."
    }

    fun onOperator(op: Char) {
        if (isError) {
            clear()
            return // nothing valid to attach the operator to
        }
        if (justEvaluated) {
            // continue chaining from the previous result instead of discarding it
            justEvaluated = false
            expression += op
            return
        }
        if (expression.isEmpty()) return // lock: no leading operator
        if (expression.last() in OPERATOR_CHARS) {
            // lock: replace a pending operator instead of stacking consecutive operators
            expression = expression.dropLast(1) + op
            return
        }
        expression += op
    }

    fun onFunction(name: String) {
        if (isError) return
        val start = expression.length - currentOperand(expression).length
        val rawOperand = expression.substring(start)
        val cleaned = rawOperand.removeSurrounding("(", ")")
        val value = cleaned.toDoubleOrNull() ?: return // lock: nothing valid to apply the function to

        val result = try {
            CalculatorEngine.applyFunction(name, value)
        } catch (e: CalculatorException) {
            isError = true
            lastEvaluatedExpression = expression
            expression = ""
            return
        }

        val formatted = CalculatorEngine.formatNumber(result)
        val precededByOperator = start > 0
        val insertText = if (result < 0 && precededByOperator) "($formatted)" else formatted
        expression = expression.substring(0, start) + insertText
        justEvaluated = false
    }

    fun onEquals() {
        if (isError) {
            clear()
            return
        }
        if (expression.isEmpty() || expression.last() in OPERATOR_CHARS) return // lock: incomplete expression

        lastEvaluatedExpression = expression
        try {
            val result = CalculatorEngine.evaluate(expression)
            expression = CalculatorEngine.formatNumber(result)
            justEvaluated = true
            isError = false
        } catch (e: CalculatorException) {
            expression = ""
            isError = true
            justEvaluated = false
        }
    }

    fun onBackspace() {
        if (isError || justEvaluated) {
            clear()
            return
        }
        if (expression.isEmpty()) return
        expression = if (expression.last() == ')') {
            val openIndex = expression.lastIndexOf('(')
            if (openIndex >= 0) expression.substring(0, openIndex) else expression.dropLast(1)
        } else {
            expression.dropLast(1)
        }
    }

    fun clear() {
        expression = ""
        isError = false
        justEvaluated = false
        lastEvaluatedExpression = ""
    }

    private fun resetIfNeeded() {
        if (isError || justEvaluated) {
            expression = ""
            isError = false
            justEvaluated = false
        }
    }

    /** Returns the substring of [expr] after the last top-level operator (the operand being typed). */
    private fun currentOperand(expr: String): String {
        var depth = 0
        for (i in expr.indices.reversed()) {
            val c = expr[i]
            when {
                c == ')' -> depth++
                c == '(' -> depth--
                depth == 0 && i != 0 && OPERATOR_CHARS.contains(c) -> return expr.substring(i + 1)
            }
        }
        return expr
    }
}
