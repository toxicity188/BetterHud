package kr.toxicity.hud.equation

import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.api.yaml.YamlObject

class EquationPairLocation(
    duration: Int,
    gui: EquationPair,
    pixel: EquationPair
) {
    companion object {
        val zero = EquationPairLocation(1, EquationPair.zero, EquationPair.zero)
    }
    val locations = (1..duration).map {
        val d = it.toDouble()
        val eval1 = gui.evaluate(d)
        val eval2 = pixel.evaluate(d)
        LocationGroup(
            GuiLocation(eval1.first, eval1.second),
            ImageLocation(eval2.first.toInt(), eval2.second.toInt())
        )
    }

    constructor(section: YamlObject): this(
        section.getAsInt("duration", 1).coerceAtLeast(1),
        section.get("gui")?.asObject()?.let {
            EquationPair(it)
        } ?: EquationPair.zero,
        section.get("pixel")?.asObject()?.let {
            EquationPair(it)
        } ?: EquationPair.zero
    )
}