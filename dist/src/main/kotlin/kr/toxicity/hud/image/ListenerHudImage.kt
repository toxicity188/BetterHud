package kr.toxicity.hud.image

import kr.toxicity.hud.manager.ListenerManager
import kr.toxicity.hud.util.ifNull
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage

class ListenerHudImage(
    name: String,
    image: List<Pair<String, BufferedImage>>,
    type: ImageType,
    val splitType: SplitType,
    setting: ConfigurationSection
): HudImage(name, image, type, setting) {
    val listener = ListenerManager.getListener(setting.getConfigurationSection("listener").ifNull("listener configuration not set."))
}