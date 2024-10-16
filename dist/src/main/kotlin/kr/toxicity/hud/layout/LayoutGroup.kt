package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.location.AnimationLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience

class LayoutGroup(
    override val path: String,
    sender: Audience,
    section: YamlObject
) : HudConfiguration {

    private val loc = PixelLocation(section)

    val align = section.get("align")?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that align: $it") {
            LayoutAlign.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutAlign.LEFT
    val offset = section.get("offset")?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that offset: $it") {
            LayoutOffset.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutOffset.CENTER

    val image = section.get("images")?.asObject()?.mapSubConfiguration { s, yamlObject ->
        ImageLayout(s, yamlObject, loc)
    } ?: emptyList()
    val text = section.get("texts")?.asObject()?.mapSubConfiguration { s, yamlObject ->
        TextLayout(s, yamlObject, loc)
    } ?: emptyList()
    val head = section.get("heads")?.asObject()?.mapSubConfiguration { s, yamlObject ->
        HeadLayout(s, yamlObject, loc)
    } ?: emptyList()

    val conditions = section.toConditions()

    val animation = section.get("animations")?.asObject()?.let { animations ->
        AnimationLocation(animations)
    } ?: AnimationLocation.zero
}