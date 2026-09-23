package com.example.calculadora

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/** Thrown for any condition that should surface as "Erro" on the display instead of crashing. */
class CalculatorException(message: String = "Erro") : Exception(message)

private sealed class Token {
    data class Num(val value: Double) : Token()
    data class Op(val symbol: Char) : Token()
}

/**
 * Parses and evaluates flat calculator expressions such as "12+5×3" or "5+(-1)^2".
 *
 * The only parentheses ever produced by the UI wrap a single negative literal coming from a
 * scientific function result (e.g. "(-1)"), so the tokenizer only needs to special-case that
 * shape rather than support arbitrary nested parenthesized sub-expressions.
 */
object CalculatorEngine {

    private const val OPERATORS = "+-×÷^"

    fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) throw CalculatorException()
        val rpn = toRpn(tokens)
        val result = evalRpn(rpn)
        if (result.isNaN() || result.isInfinite()) throw CalculatorException()
        return result
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c == '(' -> {
                    // Only shape produced by the UI: "(-<number>)"
                    val close = expr.indexOf(')', i)
                    if (close == -1) throw CalculatorException()
                    val inner = expr.substring(i + 1, close)
                    val value = inner.toDoubleOrNull() ?: throw CalculatorException()
                    tokens += Token.Num(value)
                    i = close + 1
                }
                c == '-' && i == 0 -> {
                    val (num, next) = readNumber(expr, i)
                    tokens += Token.Num(num)
                    i = next
                }
                c.isDigit() -> {
                    val (num, next) = readNumber(expr, i)
                    tokens += Token.Num(num)
                    i = next
                }
                OPERATORS.contains(c) -> {
                    tokens += Token.Op(c)
                    i++
                }
                else -> throw CalculatorException()
            }
        }
        return tokens
    }

    private fun readNumber(expr: String, start: Int): Pair<Double, Int> {
        var j = start + 1
        while (j < expr.length && (expr[j].isDigit() || expr[j] == '.')) j++
        val text = expr.substring(start, j)
        val value = text.toDoubleOrNull() ?: throw CalculatorException()
        return value to j
    }

    private fun precedence(op: Char): Int = when (op) {
        '^' -> 3
        '×', '÷' -> 2
        '+', '-' -> 1
        else -> 0
    }

    private fun isRightAssociative(op: Char) = op == '^'

    private fun toRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = ArrayDeque<Token.Op>()
        for (token in tokens) {
            when (token) {
                is Token.Num -> output += token
                is Token.Op -> {
                    while (opStack.isNotEmpty()) {
                        val top = opStack.last()
                        val shouldPop = if (isRightAssociative(token.symbol)) {
                            precedence(top.symbol) > precedence(token.symbol)
                        } else {
                            precedence(top.symbol) >= precedence(token.symbol)
                        }
                        if (shouldPop) output += opStack.removeLast() else break
                    }
                    opStack.addLast(token)
                }
            }
        }
        while (opStack.isNotEmpty()) output += opStack.removeLast()
        return output
    }

    private fun evalRpn(rpn: List<Token>): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
                is Token.Op -> {
                    if (stack.size < 2) throw CalculatorException()
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val result = when (token.symbol) {
                        '+' -> a + b
                        '-' -> a - b
                        '×' -> a * b
                        '÷' -> {
                            if (b == 0.0) throw CalculatorException()
                            a / b
                        }
                        '^' -> Math.pow(a, b)
                        else -> throw CalculatorException()
                    }
                    stack.addLast(result)
                }
            }
        }
        if (stack.size != 1) throw CalculatorException()
        return stack.last()
    }

    // ---- Scientific functions (angles in degrees) ----

    fun applyFunction(name: String, x: Double): Double {
        val result = when (name) {
            "sin" -> snapNearZero(Math.sin(Math.toRadians(x)))
            "cos" -> snapNearZero(Math.cos(Math.toRadians(x)))
            "tan" -> {
                val normalized = ((x - 90) % 180 + 180) % 180
                if (abs(normalized) < 1e-9) throw CalculatorException()
                snapNearZero(Math.tan(Math.toRadians(x)))
            }
            "log" -> {
                if (x <= 0.0) throw CalculatorException()
                Math.log10(x)
            }
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
        if (result.isNaN() || result.isInfinite()) throw CalculatorException()
        return result
    }

    private fun snapNearZero(value: Double): Double = if (abs(value) < 1e-10) 0.0 else value

    // ---- Formatting ----

    fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Erro"
        if (abs(value) >= 1e12 || (value != 0.0 && abs(value) < 1e-9)) {
            return String.format("%.6e", value)
        }
        val plain = BigDecimal(value)
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return if (plain == "-0") "0" else plain
    }
}
