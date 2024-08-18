package kr.toxicity.hud.image

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.util.toConditions
import kr.toxicity.hud.api.yaml.YamlObject

class HudImage(
    override val path: String,
    val name: String,
    val image: List<NamedLoadedImage>,
    val type: ImageType,
    setting: YamlObject
): HudConfiguration {
    val conditions = setting.toConditions()
    val listener = setting.get("listener")?.asObject()?.let {
        ListenerManagerImpl.getListener(it)
    }
}