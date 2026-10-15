package kr.toxicity.hud.animation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.TEquation

class EquationAnimation(
    private val x: TEquation,
    private val y: TEquation,
    private val opacity: TEquation,
    private val scale: TEquation,
    private val visible: TEquation = TEquation.one
) : AnimationSource {
    constructor(section: YamlObject) : this(
        section["x-equation"]?.asString()?.let(::TEquation) ?: TEquation.zero,
        section["y-equation"]?.asString()?.let(::TEquation) ?: TEquation.zero,
        section["opacity-equation"]?.asString()?.let(::TEquation) ?: TEquation.one,
        section["scale-equation"]?.asString()?.let(::TEquation) ?: TEquation.one,
        section["visible-equation"]?.asString()?.let(::TEquation) ?: TEquation.one
    )

    override fun frame(t: Int): AnimationFrame {
        val d = t.toDouble()
        return AnimationFrame(
            x evaluateToInt d,
            y evaluateToInt d,
            opacity evaluate d,
            scale evaluate d,
            (visible evaluate d) >= 0.5
        )
    }
}
