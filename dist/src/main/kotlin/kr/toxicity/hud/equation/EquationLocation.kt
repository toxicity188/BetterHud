package kr.toxicity.hud.equation

import kr.toxicity.hud.image.ImageLocation
import kotlin.math.round

class EquationLocation(
    duration: Int,
    x: String,
    y: String
) {
    val location = run {
        val xEquation = TEquation(x)
        val yEquation = TEquation(y)
        (0..<duration).map {
            val d = it.toDouble()
            ImageLocation(
                round(xEquation.evaluate(d)).toInt(),
                round(yEquation.evaluate(d)).toInt()
            )
        }
    }
}