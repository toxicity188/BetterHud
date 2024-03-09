package kr.toxicity.hud.image

import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.util.ifNull
import org.bukkit.configuration.ConfigurationSection

class ListenerHudImage(
    name: String,
    image: List<NamedLoadedImage>,
    type: ImageType,
    val splitType: SplitType,
    setting: ConfigurationSection
): HudImage(name, image, type, setting) {
    val listener = ListenerManagerImpl.getListener(setting.getConfigurationSection("listener").ifNull("listener configuration not set."))
}