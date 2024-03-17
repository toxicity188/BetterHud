package kr.toxicity.hud.player

import kr.toxicity.hud.util.toConditions
import org.bukkit.configuration.ConfigurationSection

class HudHead(val name: String, section: ConfigurationSection) {
    val pixel = section.getInt("pixel", 1).coerceAtLeast(1)
    val conditions = section.toConditions()
}