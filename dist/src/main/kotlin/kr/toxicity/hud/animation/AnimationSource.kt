package kr.toxicity.hud.animation

fun interface AnimationSource {
    fun frame(t: Int): AnimationFrame
}
