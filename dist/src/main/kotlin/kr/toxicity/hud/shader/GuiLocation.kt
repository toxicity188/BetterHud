package kr.toxicity.hud.shader

import org.bukkit.configuration.ConfigurationSection

data class GuiLocation(val x: Double, val y: Double) {
    constructor(section: ConfigurationSection): this(
        section.getDouble("x").coerceAtLeast(0.0).coerceAtMost(100.0),
        section.getDouble("y").coerceAtLeast(0.0).coerceAtMost(100.0)
    )
    operator fun plus(other: GuiLocation) = GuiLocation(x + other.x, y + other.y)
}