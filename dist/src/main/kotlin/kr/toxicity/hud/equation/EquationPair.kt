package kr.toxicity.hud.equation

import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toEquation
import org.bukkit.configuration.ConfigurationSection

class EquationPair(val x: TEquation, val y: TEquation) {
    companion object {
        val zero = EquationPair(TEquation.zero, TEquation.zero)
    }

    fun evaluate(d: Double) = x.evaluate(d) to y.evaluate(d)

    constructor(section: ConfigurationSection): this(
        section.getString("x-equation").ifNull("x-equation value not set.").toEquation(),
        section.getString("y-equation").ifNull("y-equation value not set.").toEquation()
    )
}