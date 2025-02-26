package kr.toxicity.hud.equation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toEquation

class EquationTriple(val x: TEquation, val y: TEquation, val opacity: TEquation) {
    companion object {
        val zero = EquationTriple(TEquation.zero, TEquation.zero, TEquation.one)
    }

    infix fun evaluate(d: Double) = Triple(
        x evaluate d,
        y evaluate d,
        opacity evaluate d
    )

    constructor(section: YamlObject): this(
        section["x-equation"]?.asString().ifNull { "x-equation value not set." }.toEquation(),
        section["y-equation"]?.asString().ifNull { "y-equation value not set." }.toEquation(),
        section["opacity-equation"]?.asString()?.toEquation() ?: TEquation.one
    )
}