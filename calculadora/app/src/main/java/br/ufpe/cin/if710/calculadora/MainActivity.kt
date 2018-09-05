package br.ufpe.cin.if710.calculadora

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var stringBuilder = StringBuilder()
        var shouldClear = true

        fun appendText(text: CharSequence) {
            if (shouldClear) {
                text_calc.setText(text)
                shouldClear = false
            } else text_calc.append(text)
            stringBuilder.append(text)
        }
        fun appendOperator(text: CharSequence, shouldPrint: Boolean) {
            if(shouldPrint && shouldClear) {
                text_calc.setText(text)
                shouldClear = false
            } else {
                shouldClear = true
            }
            stringBuilder.append(text)
        }

        fun appendOperator(text: CharSequence) {
            appendOperator(text, false)
        }

        setContentView(R.layout.activity_main)
        btn_0.setOnClickListener { appendText("0") }
        btn_1.setOnClickListener { appendText("1") }
        btn_2.setOnClickListener { appendText("2") }
        btn_3.setOnClickListener { appendText("3") }
        btn_4.setOnClickListener { appendText("4") }
        btn_5.setOnClickListener { appendText("5") }
        btn_6.setOnClickListener { appendText("6") }
        btn_7.setOnClickListener { appendText("7") }
        btn_8.setOnClickListener { appendText("8") }
        btn_9.setOnClickListener { appendText("9") }
        btn_Dot.setOnClickListener {appendText(".") }
        btn_LParen.setOnClickListener {
            stringBuilder.append("(")
            shouldClear = true
        }
        btn_RParen.setOnClickListener { appendOperator(")") }
        btn_Power.setOnClickListener { appendOperator("^") }
        btn_Add.setOnClickListener { appendOperator("+") }
        btn_Subtract.setOnClickListener { appendOperator("-", true) }
        btn_Multiply.setOnClickListener { appendOperator("*") }
        btn_Divide.setOnClickListener { appendOperator("/") }
        btn_Equal.setOnClickListener {
            try {
                var result = eval(stringBuilder.toString())
                stringBuilder.setLength(0)
                text_info.text = result.toString()
                shouldClear = true
            } catch (ex: RuntimeException) {
                Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
        btn_Clear.setOnClickListener {
            text_calc.setText("0")
            stringBuilder.setLength(0)
            shouldClear = true
        }
    }

    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
