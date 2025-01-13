package kr.toxicity.hud.equation

import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.lang.Math.clamp
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

class TEquation(expression: String) {
    companion object {
        val t = TEquation("t")
        val one = TEquation("1")
        val zero = TEquation("0")
    }

    private val expression = ExpressionBuilder(expression)
        .functions(
            object : Function("min", 2) {
                override fun apply(vararg p0: Double): Double = min(p0[0], p0[1])
            },
            object : Function("max", 2) {
                override fun apply(vararg p0: Double): Double = max(p0[0], p0[1])
            },
            object : Function("clamp", 3) {
                override fun apply(vararg p0: Double): Double = clamp(p0[0], p0[1], p0[2])
            }
        )
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