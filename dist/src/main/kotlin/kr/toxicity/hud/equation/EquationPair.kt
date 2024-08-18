package kr.toxicity.hud.equation

import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toEquation
import kr.toxicity.hud.api.yaml.YamlObject

class EquationPair(val x: TEquation, val y: TEquation) {
    companion object {
        val zero = EquationPair(TEquation.zero, TEquation.zero)
    }

    fun evaluate(d: Double) = x.evaluate(d) to y.evaluate(d)

    constructor(section: YamlObject): this(
        section.get("x-equation")?.asString().ifNull("x-equation value not set.").toEquation(),
        section.get("y-equation")?.asString().ifNull("y-equation value not set.").toEquation()
    )
}