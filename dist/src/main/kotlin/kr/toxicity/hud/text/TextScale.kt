package kr.toxicity.hud.text

import kotlin.math.roundToInt

class TextScale(
    val width: Double,
    val height: Double
) {
    operator fun times(scale: Double) = TextScale(
        width * scale,
        height * scale
    )
    val normalizedHeight
        get() = height.roundToInt()

    val normalizedWidth
        get() = (width / height * normalizedHeight).roundToInt()
}