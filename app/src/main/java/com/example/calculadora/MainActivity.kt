package com.example.calculadora

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadora.ui.theme.CalculadoraTheme
import androidx.compose.ui.platform.LocalConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme(darkTheme = false, dynamicColor = false) {
                CalculatorScreen()
            }
        }
    }
}

// Purple & white theme, with a warm gold accent on "=" for contrast.
private val BackgroundColor = Color(0xFFFFFFFF)
private val DisplayBigText = Color(0xFF2E0854)
private val DisplaySmallText = Color(0xFF9B8AC4)
private val DigitBg = Color(0xFFF1EAFB)
private val DigitText = Color(0xFF2E0854)
private val DigitBorder = Color(0xFFE0D0F5)
private val OperatorBg = Color(0xFF7C3AED)
private val OperatorText = Color.White
private val FunctionBg = Color(0xFFC9A9FF)
private val FunctionText = Color(0xFF2E0854)
private val UtilityBg = Color(0xFFD9CCF0)
private val UtilityText = Color(0xFF2E0854)
private val EqualsBg = Color(0xFFFFB300)
private val EqualsText = Color(0xFF2E0854)
private val ErrorColor = Color(0xFFD32F2F)

@Composable
fun CalculatorScreen() {
    val state = remember { CalculatorState() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        DisplaySection(state = state, modifier = Modifier.weight(1f))

        if (isLandscape) {
            Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScientificColumn(state = state, modifier = Modifier.weight(1f))
                ButtonGrid(state = state, modifier = Modifier.weight(3f))
            }
        } else {
            ButtonGrid(state = state, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun DisplaySection(state: CalculatorState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = state.smallDisplay,
            color = DisplaySmallText,
            fontSize = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        Text(
            text = state.bigDisplay,
            color = if (state.isError) ErrorColor else DisplayBigText,
            fontSize = if (state.bigDisplay.length > 9) 40.sp else 56.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun ScientificColumn(state: CalculatorState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CalcButton("sin", Modifier.weight(1f).fillMaxWidth(), FunctionBg, FunctionText) { state.onFunction("sin") }
        CalcButton("cos", Modifier.weight(1f).fillMaxWidth(), FunctionBg, FunctionText) { state.onFunction("cos") }
        CalcButton("tan", Modifier.weight(1f).fillMaxWidth(), FunctionBg, FunctionText) { state.onFunction("tan") }
        CalcButton("log", Modifier.weight(1f).fillMaxWidth(), FunctionBg, FunctionText) { state.onFunction("log") }
    }
}

@Composable
private fun ButtonGrid(state: CalculatorState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        GridRow(Modifier.weight(1f)) {
            CalcButton("C", Modifier.weight(1f).fillMaxHeight(), UtilityBg, UtilityText, DigitBorder) { state.clear() }
            CalcButton("⌫", Modifier.weight(1f).fillMaxHeight(), UtilityBg, UtilityText, DigitBorder) { state.onBackspace() }
            CalcButton("^", Modifier.weight(1f).fillMaxHeight(), OperatorBg, OperatorText) { state.onOperator('^') }
            CalcButton("÷", Modifier.weight(1f).fillMaxHeight(), OperatorBg, OperatorText) { state.onOperator('÷') }
        }
        GridRow(Modifier.weight(1f)) {
            CalcButton("7", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("7") }
            CalcButton("8", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("8") }
            CalcButton("9", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("9") }
            CalcButton("×", Modifier.weight(1f).fillMaxHeight(), OperatorBg, OperatorText) { state.onOperator('×') }
        }
        GridRow(Modifier.weight(1f)) {
            CalcButton("4", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("4") }
            CalcButton("5", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("5") }
            CalcButton("6", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("6") }
            CalcButton("-", Modifier.weight(1f).fillMaxHeight(), OperatorBg, OperatorText) { state.onOperator('-') }
        }
        GridRow(Modifier.weight(1f)) {
            CalcButton("1", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("1") }
            CalcButton("2", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("2") }
            CalcButton("3", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("3") }
            CalcButton("+", Modifier.weight(1f).fillMaxHeight(), OperatorBg, OperatorText) { state.onOperator('+') }
        }
        GridRow(Modifier.weight(1f)) {
            CalcButton("0", Modifier.weight(2f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDigit("0") }
            CalcButton(".", Modifier.weight(1f).fillMaxHeight(), DigitBg, DigitText, DigitBorder) { state.onDecimal() }
            CalcButton("=", Modifier.weight(1f).fillMaxHeight(), EqualsBg, EqualsText) { state.onEquals() }
        }
    }
}

@Composable
private fun GridRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculadoraTheme(darkTheme = false, dynamicColor = false) {
        CalculatorScreen()
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360)
@Composable
fun CalculatorPreviewLandscape() {
    CalculadoraTheme(darkTheme = false, dynamicColor = false) {
        CalculatorScreen()
    }
}
