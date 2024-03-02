package kr.toxicity.hud.equation

import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.E
import kotlin.math.PI

class TEquation(expression: String) {
    companion object {
        val zero = TEquation("0")
    }

    private val expression = ExpressionBuilder(expression)
        .variables(setOf(
            "t",
            "pi",
            "e"
        ))
        .build()

    fun evaluate(t: Double) = Expression(expression)
        .setVariables(mapOf(
            "t" to t,
            "pi" to PI,
            "e" to E
        ))
        .evaluate()
}