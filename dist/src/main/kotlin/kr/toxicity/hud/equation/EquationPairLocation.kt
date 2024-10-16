package kr.toxicity.hud.equation

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.location.GuiLocation

class EquationPairLocation(
    duration: Int,
    gui: EquationPair,
    pixel: EquationTriple
) {
    companion object {
        val zero = EquationPairLocation(1, EquationPair.zero, EquationTriple.zero)
    }
    val locations = (1..duration).map {
        val d = it.toDouble()
        val eval1 = gui.evaluate(d)
        val eval2 = pixel.evaluate(d)
        LocationGroup(
            GuiLocation(eval1.first, eval1.second),
            PixelLocation(eval2.first.toInt(), eval2.second.toInt(), eval2.third)
        )
    }

    constructor(section: YamlObject): this(
        section.getAsInt("duration", 1).coerceAtLeast(1),
        section.get("gui")?.asObject()?.let {
            EquationPair(it)
        } ?: EquationPair.zero,
        section.get("pixel")?.asObject()?.let {
            EquationTriple(it)
        } ?: EquationTriple.zero
    )
}