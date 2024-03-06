package kr.toxicity.hud.image

import org.bukkit.configuration.ConfigurationSection

data class ImageLocation(val x: Int, val y: Int) {
    companion object {
        val zero = ImageLocation(0, 0)
    }
    constructor(section: ConfigurationSection): this(
        section.getInt("x"),
        section.getInt("y")
    )
    operator fun plus(other: ImageLocation) = ImageLocation(x + other.x, y + other.y)
}