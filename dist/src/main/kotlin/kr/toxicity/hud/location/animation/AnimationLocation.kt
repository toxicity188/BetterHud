package kr.toxicity.hud.location.animation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.EquationTriple
import kr.toxicity.hud.location.PixelLocation
import kotlin.math.roundToInt

class AnimationLocation(
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
                imageEquation.x.evaluate(d).roundToInt(),
                imageEquation.y.evaluate(d).roundToInt(),
                imageEquation.opacity.evaluate(d)
            )
        }
    )

    constructor(section: YamlObject): this(
        section.get("type")?.asString()?.let {
            AnimationType.valueOf(it.uppercase())
        } ?: AnimationType.LOOP,
        section.getAsInt("duration", 20).coerceAtLeast(1),
        EquationTriple(section)
    )
}