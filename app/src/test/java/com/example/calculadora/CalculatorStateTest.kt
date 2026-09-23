package com.example.calculadora

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorStateTest {

    @Test
    fun `typing digits builds the expression`() {
        val state = CalculatorState()
        state.onDigit("1")
        state.onDigit("2")
        assertEquals("12", state.bigDisplay)
    }

    @Test
    fun `consecutive operators replace the pending one instead of stacking`() {
        val state = CalculatorState()
        state.onDigit("5")
        state.onOperator('+')
        state.onOperator('×') // should replace '+', not append
        state.onOperator('-') // should replace '×', not append
        state.onDigit("3")
        assertEquals("5-3", state.smallDisplay)
    }

    @Test
    fun `leading operator is ignored`() {
        val state = CalculatorState()
        state.onOperator('+')
        assertEquals("0", state.bigDisplay)
        assertEquals("", state.smallDisplay)
    }

    @Test
    fun `a second decimal point in the same number is ignored`() {
        val state = CalculatorState()
        state.onDigit("1")
        state.onDecimal()
        state.onDigit("5")
        state.onDecimal() // locked: "1.5" already has a dot
        state.onDigit("7")
        assertEquals("1.57", state.bigDisplay)
    }

    @Test
    fun `decimal point after an operator starts a new fractional number`() {
        val state = CalculatorState()
        state.onDigit("1")
        state.onOperator('+')
        state.onDecimal()
        state.onDigit("5")
        assertEquals("1+0.5", state.smallDisplay)
    }

    @Test
    fun `division by zero shows Erro and does not crash`() {
        val state = CalculatorState()
        state.onDigit("5")
        state.onOperator('÷')
        state.onDigit("0")
        state.onEquals()
        assertEquals(true, state.isError)
        assertEquals("Erro", state.bigDisplay)
    }

    @Test
    fun `typing after an error starts a fresh expression`() {
        val state = CalculatorState()
        state.onDigit("5")
        state.onOperator('÷')
        state.onDigit("0")
        state.onEquals()
        state.onDigit("9")
        assertEquals(false, state.isError)
        assertEquals("9", state.bigDisplay)
    }

    @Test
    fun `equals respects operator precedence and allows chaining`() {
        val state = CalculatorState()
        "2+3×4".forEach { c ->
            when {
                c.isDigit() -> state.onDigit(c.toString())
                else -> state.onOperator(c)
            }
        }
        state.onEquals()
        assertEquals("14", state.bigDisplay)
        state.onOperator('+')
        state.onDigit("1")
        assertEquals("14+1", state.smallDisplay)
    }

    @Test
    fun `scientific function applies to the number currently typed`() {
        val state = CalculatorState()
        state.onDigit("9")
        state.onDigit("0")
        state.onFunction("sin")
        assertEquals("1", state.bigDisplay)
    }

    @Test
    fun `equals is locked when the expression ends in an operator`() {
        val state = CalculatorState()
        state.onDigit("5")
        state.onOperator('+')
        state.onEquals() // incomplete expression, must be ignored
        assertEquals(false, state.isError)
        assertEquals("5+", state.smallDisplay)
    }
}
