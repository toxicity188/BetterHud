package kr.toxicity.hud.equation

import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toEquation
import org.bukkit.configuration.ConfigurationSection
import kotlin.math.round

class EquationLocation(
    duration: Int,
    equationPair: EquationPair
) {

    val location = run {
        (0..<duration).map {
            val d = it.toDouble()
            ImageLocation(
                round(equationPair.x.evaluate(d)).toInt(),
                round(equationPair.y.evaluate(d)).toInt()
            )
        }
    }

    constructor(section: ConfigurationSection): this(
        section.getInt("duration", 20).coerceAtLeast(1),
        EquationPair(section)
    )
}