package kr.toxicity.hud.player.head

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.util.toConditions

class HudHead(
    override val path: String,
    val name: String,
    section: YamlObject
): HudConfiguration {
    val pixel = section.getAsInt("pixel", 1).coerceAtLeast(1)
    val conditions = section.toConditions()
}