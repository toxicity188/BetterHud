package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.shader.RenderScale
import kr.toxicity.hud.util.toConditions

abstract class HudLayout(originalLoc: ImageLocation, yaml: YamlObject) {
    val outline: Boolean = yaml.getAsBoolean("outline", false)
    val layer: Int = yaml.getAsInt("layer", 0)
    val follow: String? = yaml.get("follow")?.asString()
    val location: ImageLocation = ImageLocation(yaml) + originalLoc + ImageLocation.hotBarHeight
    val cancelIfFollowerNotExists: Boolean = yaml.getAsBoolean("cancel-if-follower-not-exists", true)
    val conditions: ConditionBuilder = yaml.toConditions()
    val renderScale = RenderScale.fromConfig(location, yaml)
}