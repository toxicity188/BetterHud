package kr.toxicity.hud.image

import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.util.toConditions
import org.bukkit.configuration.ConfigurationSection

class HudImage(
    val name: String,
    val image: List<NamedLoadedImage>,
    val type: ImageType,
    setting: ConfigurationSection
) {
    val conditions = setting.toConditions()
    val listener = setting.getConfigurationSection("listener")?.let {
        ListenerManagerImpl.getListener(it)
    }
}