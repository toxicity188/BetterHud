package kr.toxicity.hud.animation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.EquationTriple
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.util.getAsAnimationType

data class AnimationLocation(
    val type: AnimationType,
    val location: List<PixelLocation>
) {
    companion object {
        val zero = AnimationLocation(AnimationType.LOOP, listOf(PixelLocation.zero))
    }
    constructor(
        type: AnimationType,
        duration: Int,
        imageEquation: EquationTriple
    ): this(
        type,
        (0..<duration).map {
            val d = it.toDouble()
            PixelLocation(
                imageEquation.x evaluateToInt d,
                imageEquation.y evaluateToInt d,
                imageEquation.opacity evaluate d
            )
        }
    )

    constructor(section: YamlObject): this(
        section.getAsAnimationType("type"),
        section.getAsInt("duration", 20).coerceAtLeast(1),
        EquationTriple(section)
    )
}