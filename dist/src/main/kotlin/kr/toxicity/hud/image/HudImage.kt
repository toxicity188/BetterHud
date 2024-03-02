package kr.toxicity.hud.image

import kr.toxicity.hud.placeholder.Conditions
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage

open class HudImage(
    val name: String,
    val image: List<Pair<String, BufferedImage>>,
    val type: ImageType,
    setting: ConfigurationSection
) {
    val conditions = setting.getConfigurationSection("conditions")?.let {
        Conditions.parse(it)
    } ?: { true }
}