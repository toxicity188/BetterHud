package kr.toxicity.hud.shader

import kr.toxicity.hud.api.yaml.YamlObject

data class GuiLocation(val x: Double, val y: Double) : Comparable<GuiLocation> {
    companion object {
        private val comparator = Comparator.comparing { gui: GuiLocation ->
            gui.x
        }.thenComparing { gui: GuiLocation ->
            gui.y
        }

        val one = GuiLocation(1.0, 1.0)
    }

    constructor(section: YamlObject): this(
        section.getAsDouble("x", 0.0).coerceAtLeast(0.0).coerceAtMost(100.0),
        section.getAsDouble("y", 0.0).coerceAtLeast(0.0).coerceAtMost(100.0)
    )
    operator fun plus(other: GuiLocation) = GuiLocation(x + other.x, y + other.y)
    override fun compareTo(other: GuiLocation): Int = comparator.compare(this, other)
}