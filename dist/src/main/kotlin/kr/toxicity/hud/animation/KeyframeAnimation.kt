package kr.toxicity.hud.animation

import kotlin.math.roundToInt

class KeyframeAnimation(keyframes: List<Keyframe>) : AnimationSource {
    init {
        require(keyframes.isNotEmpty()) { "keyframes cannot be empty" }
    }

    private val sorted = keyframes.sortedBy { it.t }

    val lastTime = sorted.last().t
    val duration get() = lastTime + 1

    private val x = Channel(sorted, 0.0) { it.x }
    private val y = Channel(sorted, 0.0) { it.y }
    private val opacity = Channel(sorted, 1.0) { it.opacity }
    private val scale = Channel(sorted, 1.0) { it.scale }
    private val visible = Channel(sorted.map { it.copy(easing = Easing.STEP) }, 1.0) { k -> k.visible?.let { if (it) 1.0 else 0.0 } }

    override fun frame(t: Int) = AnimationFrame(
        x.at(t).roundToInt(),
        y.at(t).roundToInt(),
        opacity.at(t),
        scale.at(t),
        visible.at(t) >= 0.5
    )

    private class Channel(keyframes: List<Keyframe>, private val default: Double, selector: (Keyframe) -> Double?) {
        private class Point(val t: Int, val value: Double, val easing: Easing)

        private val points = keyframes.mapNotNull { k ->
            selector(k)?.let { Point(k.t, it, k.easing) }
        }

        fun at(t: Int): Double {
            if (points.isEmpty()) return default
            if (t <= points.first().t) return points.first().value
            if (t >= points.last().t) return points.last().value
            val nextIndex = points.indexOfFirst { it.t > t }
            val from = points[nextIndex - 1]
            val to = points[nextIndex]
            val k = (t - from.t).toDouble() / (to.t - from.t)
            return to.easing.interpolate(from.value, to.value, k)
        }
    }
}
