package kr.toxicity.hud.animation

import kotlin.math.roundToInt

data class AnimationQuantizer(
    val opacityStep: Double = DEFAULT_OPACITY_STEP,
    val scaleStep: Double = DEFAULT_SCALE_STEP
) {
    companion object {
        const val DEFAULT_OPACITY_STEP = 0.125
        const val DEFAULT_SCALE_STEP = 0.05
        val none = AnimationQuantizer(0.0, 0.0)
    }

    init {
        require(opacityStep >= 0.0) { "opacity-step cannot be negative" }
        require(scaleStep >= 0.0) { "scale-step cannot be negative" }
    }

    fun opacity(value: Double) = value.coerceIn(0.0, 1.0).quantize(opacityStep)

    fun scale(value: Double) = value.coerceAtLeast(0.0).quantize(scaleStep)

    fun quantize(frame: AnimationFrame) = if (!frame.visible) AnimationFrame.hidden else frame.copy(
        opacity = opacity(frame.opacity),
        scale = scale(frame.scale)
    )

    private fun Double.quantize(step: Double) = if (step <= 0.0) this else {
        ((this / step).roundToInt() * step).let { q ->
            (q * 1_000_000).roundToInt() / 1_000_000.0
        }
    }
}
