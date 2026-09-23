package com.example.calculadora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.math.abs

class CalculatorEngineTest {

    private fun assertResult(expected: Double, expression: String, epsilon: Double = 1e-9) {
        val actual = CalculatorEngine.evaluate(expression)
        assertEquals("evaluating \"$expression\"", expected, actual, epsilon)
    }

    @Test
    fun `basic arithmetic`() {
        assertResult(4.0, "2+2")
        assertResult(6.0, "10-4")
        assertResult(21.0, "3×7")
        assertResult(4.0, "12÷3")
    }

    @Test
    fun `respects operator precedence`() {
        assertResult(14.0, "2+3×4")
        assertResult(10.0, "2×3+4")
        assertResult(7.0, "1+3×2^1")
    }

    @Test
    fun `power is right associative and higher precedence`() {
        assertResult(8.0, "2^3")
        assertResult(512.0, "2^3^2") // 2^(3^2) = 2^9
    }

    @Test
    fun `handles negative literals from scientific functions`() {
        assertResult(4.0, "5+(-1)")
        assertResult(-5.0, "(-5)")
        assertResult(-6.0, "(-2)×3")
    }

    @Test
    fun `division by zero throws instead of crashing`() {
        assertThrows(CalculatorException::class.java) {
            CalculatorEngine.evaluate("5÷0")
        }
    }

    @Test
    fun `trig functions in degrees`() {
        assertEquals(0.0, CalculatorEngine.applyFunction("sin", 0.0), 1e-9)
        assertEquals(1.0, CalculatorEngine.applyFunction("sin", 90.0), 1e-9)
        assertEquals(1.0, CalculatorEngine.applyFunction("cos", 0.0), 1e-9)
        assertEquals(0.0, CalculatorEngine.applyFunction("cos", 90.0), 1e-9)
        assertEquals(1.0, CalculatorEngine.applyFunction("tan", 45.0), 1e-9)
    }

    @Test
    fun `tan is undefined at 90 degrees`() {
        assertThrows(CalculatorException::class.java) {
            CalculatorEngine.applyFunction("tan", 90.0)
        }
    }

    @Test
    fun `log of non-positive number throws`() {
        assertThrows(CalculatorException::class.java) {
            CalculatorEngine.applyFunction("log", 0.0)
        }
        assertThrows(CalculatorException::class.java) {
            CalculatorEngine.applyFunction("log", -3.0)
        }
        assertEquals(2.0, CalculatorEngine.applyFunction("log", 100.0), 1e-9)
    }

    @Test
    fun `formatNumber strips trailing zeros`() {
        assertEquals("4", CalculatorEngine.formatNumber(4.0))
        assertEquals("4.5", CalculatorEngine.formatNumber(4.5))
        assertEquals("0", CalculatorEngine.formatNumber(-0.0))
    }
}
