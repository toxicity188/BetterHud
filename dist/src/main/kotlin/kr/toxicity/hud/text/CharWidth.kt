package kr.toxicity.hud.text

import kotlin.math.roundToInt

open class CharWidth(val width: Int, val height: Int) {
    fun scaledWidth(scale: Double) = (width.toDouble() / height.toDouble() * (height * scale).roundToInt()).roundToInt()
}