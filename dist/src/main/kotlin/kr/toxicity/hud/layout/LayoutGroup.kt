package kr.toxicity.hud.layout

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.layout.enums.LayoutFlow
import kr.toxicity.hud.layout.enums.LayoutOffset
import kr.toxicity.hud.animation.AnimationLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.util.*

class LayoutGroup(
    override val id: String,
    sender: BetterCommandSource,
    section: YamlObject
) : HudConfiguration, ConditionSource by ConditionSource.Impl(section) {

    private val loc = PixelLocation(section)

    val align = section["align"]?.asString()?.let {
        runCatching {
            LayoutAlign.valueOf(it.uppercase())
        }.onFailure {
            it.handle(sender, "Unable to find that align: $it")
        }.getOrNull()
    } ?: LayoutAlign.LEFT
    val offset = section["offset"]?.asString()?.let {
        runCatching {
            LayoutOffset.valueOf(it.uppercase())
        }.onFailure {
            it.handle(sender, "Unable to find that offset: $it")
        }.getOrNull()
    } ?: LayoutOffset.CENTER
    val flow = section["flow"]?.asString()?.let {
        runCatching {
            LayoutFlow.valueOf(it.uppercase())
        }.onFailure {
            it.handle(sender, "Unable to find that flow: $it")
        }.getOrNull()
    } ?: LayoutFlow.NONE
    val flowGap = section.getAsInt("flow-gap", 0)

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