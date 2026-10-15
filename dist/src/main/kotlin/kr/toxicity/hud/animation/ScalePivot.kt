package kr.toxicity.hud.animation

import kotlin.math.roundToInt

enum class ScalePivot(private val xRatio: Double, private val yRatio: Double) {
    CENTER(0.5, 0.5),
    TOP_LEFT(0.0, 0.0),
    TOP(0.5, 0.0),
    TOP_RIGHT(1.0, 0.0),
    LEFT(0.0, 0.5),
    RIGHT(1.0, 0.5),
    BOTTOM_LEFT(0.0, 1.0),
    BOTTOM(0.5, 1.0),
    BOTTOM_RIGHT(1.0, 1.0)
    ;

    data class Offset(val x: Int, val y: Int)

    fun offset(baseWidth: Double, baseHeight: Double, scale: Double) = Offset(
        (baseWidth * (1 - scale) * xRatio).roundToInt(),
        (baseHeight * (1 - scale) * yRatio).roundToInt()
    )

    companion object {
        fun parse(name: String?): ScalePivot = if (name == null) CENTER else runCatching {
            valueOf(name.uppercase().replace('-', '_'))
        }.getOrElse { throw RuntimeException("This pivot doesn't exist: $name") }
    }
}
