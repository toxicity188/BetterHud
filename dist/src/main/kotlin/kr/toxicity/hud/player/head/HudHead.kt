package kr.toxicity.hud.player.head

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.util.toConditions
import org.bukkit.configuration.ConfigurationSection

class HudHead(
    override val path: String,
    val name: String,
    section: ConfigurationSection
): HudConfiguration {
    val pixel = section.getInt("pixel", 1).coerceAtLeast(1)
    val conditions = section.toConditions()
}