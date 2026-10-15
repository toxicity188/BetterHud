package kr.toxicity.hud.animation

import kr.toxicity.hud.location.PixelLocation

data class AnimationFrame(
    val x: Int,
    val y: Int,
    val opacity: Double,
    val scale: Double,
    val visible: Boolean = true
) {
    companion object {
        val identity = AnimationFrame(0, 0, 1.0, 1.0)
        val hidden = AnimationFrame(0, 0, 1.0, 1.0, false)
    }

    val pixel get() = PixelLocation(x, y, opacity)
    val isIdentity get() = this == identity
}
