package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.layout.enums.LayoutOffset
import kr.toxicity.hud.location.animation.AnimationLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience

class LayoutGroup(
    override val id: String,
    sender: Audience,
    section: YamlObject
) : HudConfiguration, ConditionSource by ConditionSource.Impl(section) {

    private val loc = PixelLocation(section)

    val align = section["align"]?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that align: $it") {
            LayoutAlign.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutAlign.LEFT
    val offset = section["offset"]?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that offset: $it") {
            LayoutOffset.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutOffset.CENTER

    val image = section["images"]?.asObject()?.mapSubConfiguration { s, yamlObject ->
        ImageLayout.Impl(s, this, yamlObject, loc)
    } ?: emptyList()
    val text = section["texts"]?.asObject()?.mapSubConfiguration { s, yamlObject ->
        TextLayout.Impl(s, this, yamlObject, loc)
    } ?: emptyList()
    val head = section["heads"]?.asObject()?.mapSubConfiguration { s, yamlObject ->
        HeadLayout.Impl(s, this, yamlObject, loc)
    } ?: emptyList()

    val animation = section["animations"]?.asObject()?.let { animations ->
        AnimationLocation(animations)
    } ?: AnimationLocation.zero
}