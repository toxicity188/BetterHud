package kr.toxicity.hud.image

import kr.toxicity.hud.util.toConditions
import org.bukkit.configuration.ConfigurationSection

open class HudImage(
    val name: String,
    val image: List<NamedLoadedImage>,
    val type: ImageType,
    setting: ConfigurationSection
) {
    val conditions = setting.toConditions()
}