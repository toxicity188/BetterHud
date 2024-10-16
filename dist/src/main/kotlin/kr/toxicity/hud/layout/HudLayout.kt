package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.shader.RenderScale
import kr.toxicity.hud.shader.ShaderProperty
import kr.toxicity.hud.util.toConditions

abstract class HudLayout(originalLoc: PixelLocation, yaml: YamlObject) {
    val outline: Boolean = yaml.getAsBoolean("outline", false)
    val layer: Int = yaml.getAsInt("layer", 0)
    val property: Int = ShaderProperty.properties(yaml.get("properties")?.asArray())
    val follow: String? = yaml.get("follow")?.asString()
    val location: PixelLocation = PixelLocation(yaml) + originalLoc + PixelLocation.hotBarHeight
    val cancelIfFollowerNotExists: Boolean = yaml.getAsBoolean("cancel-if-follower-not-exists", true)
    val conditions: ConditionBuilder = yaml.toConditions()
    val renderScale = RenderScale.fromConfig(location, yaml)
}