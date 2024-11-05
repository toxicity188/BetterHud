package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.text.CharWidth
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.text.ImageCharWidth
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor

class TextRenderer(
    private val widthMap: Map<Int, CharWidth>,
    private val imageWidthMap: Map<Int, ImageCharWidth>,
    private val defaultColor: TextColor,
    private val data: HudTextData,
    pattern: String,
    private val align: LayoutAlign,
    private val scale: Double,
    private val x: Int,

    private val numberEquation: TEquation,
    private val numberPattern: DecimalFormat,
    private val disableNumberFormat: Boolean,

    follow: String?,
    private val cancelIfFollowerNotExists: Boolean,

    private val useLegacyFormat: Boolean,
    private val legacySerializer: ComponentDeserializer,
    private val space: Int,
    private val condition: ConditionBuilder
) {
    companion object {
        private val decimalPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val allPattern = Pattern.compile(".+")
        private val imagePattern = Pattern.compile("<(?<type>(image|space)):(?<name>(([a-zA-Z]|[0-9]|-)+))>")
    }

    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    private val parsedPatter = PlaceholderManagerImpl.parse(pattern)

    private val widthViewer = ValueViewer<Pair<Style, Int>, Int>()
        .addFunction(
            {
                when (it.first.font()) {
                    SPACE_KEY -> it.second - CURRENT_CENTER_SPACE_CODEPOINT
                    LEGACY_SPACE_KEY -> it.second - LEGACY_CENTER_SPACE_CODEPOINT
                    null -> when (it.second) {
                        TEXT_SPACE_KEY_CODEPOINT -> space
                        else -> (widthMap[it.second] ?: imageWidthMap[it.second])?.let { c -> c.scaledWidth(scale) + 1 }
                    }
                    else -> null
                }
            }
        )


    fun getText(reason: UpdateEvent): (HudPlayer) -> PixelComponent {
        val buildPattern = parsedPatter(reason)
        val cond = condition.build(reason)

        val followTarget = followHudPlayer?.build(reason)

        return build@ { hudPlayer ->
            var targetHudPlayer = hudPlayer
            followTarget?.let {
                PlayerManagerImpl.getHudPlayer(it.value(hudPlayer).toString())?.let { p ->
                    targetHudPlayer = p
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
            }
            if (!cond(targetHudPlayer)) return@build EMPTY_PIXEL_COMPONENT

            var widthComp = EMPTY_WIDTH_COMPONENT

            val compList = buildPattern(targetHudPlayer)
                .parseToComponent()
                .split(data.splitWidth, space, widthViewer)
            compList.forEachIndexed { index, comp ->
                if (data.font.lastIndex < index) return@forEachIndexed
                comp.component.font(data.font[index])
                widthComp = if (widthComp.width == 0) comp else widthComp plusWithAlign comp
            }
            widthComp.toPixelComponent(when (align) {
                LayoutAlign.LEFT -> x
                LayoutAlign.CENTER -> x - widthComp.width / 2
                LayoutAlign.RIGHT -> x - widthComp.width
            })
        }
    }

    private infix fun WidthComponent.plusWithAlign(other: WidthComponent) = plusWithAlign(LayoutAlign.CENTER, other)
    private fun WidthComponent.plusWithAlign(align: LayoutAlign, other: WidthComponent) = when (align) {
        LayoutAlign.LEFT -> this + (-width).toSpaceComponent() + other
        LayoutAlign.CENTER -> {
            if (width > other.width) {
                val div = (width - other.width).toDouble() / 2
                this + floor(-width + div).toInt().toSpaceComponent() + other + ceil(div).toInt().toSpaceComponent()
            } else {
                val div = floor((other.width - width).toDouble() / 2).toInt()
                div.toSpaceComponent() + this + (-width - div).toSpaceComponent() + other
            }
        }
        LayoutAlign.RIGHT -> {
            if (width > other.width) {
                val div = width - other.width
                this + (-width + div).toSpaceComponent() + other
            } else {
                val div = other.width - width
                div.toSpaceComponent() + this + (-width - div).toSpaceComponent() + other
            }
        }
    }

    private fun String.parseToComponent(): Component {
        var targetString = (if (useLegacyFormat) legacySerializer(this) else Component.text(this))
            .color(defaultColor)
            .replaceText(TextReplacementConfig.builder()
                .match(imagePattern)
                .replacement { r, _ ->
                    when (r.group(1)) {
                        "image" -> data.imageCodepoint[r.group(3)]?.let { Component.text(it.parseChar()) } ?: Component.empty()
                        "space" -> r.group(3).toIntOrNull()?.toSpaceComponent()?.component ?: Component.empty()
                        else -> Component.empty()
                    }
                }
                .build())
            .replaceText(TextReplacementConfig.builder()
                .match(allPattern)
                .replacement { r, _ ->
                    MiniMessage.miniMessage().deserialize(r.group())
                }
                .build())
        if (!disableNumberFormat) {
            targetString = targetString.replaceText(TextReplacementConfig.builder()
                .match(decimalPattern)
                .replacement { r, _ ->
                    val g = r.group()
                    Component.text(runCatching {
                        numberPattern.format(numberEquation.evaluate(g.toDouble()))
                    }.getOrDefault(g))
                }
                .build())
        }
        return targetString
    }
}