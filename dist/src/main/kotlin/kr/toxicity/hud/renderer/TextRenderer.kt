package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.tickProvide
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor

class TextRenderer(
    layout: TextLayout,
    private val data: HudTextData,
    private val x: Int,
) : TextLayout by layout, Renderer {

    companion object {
        private val decimalPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val allPattern = Pattern.compile(".+")
        private val emptyTag by lazy {
            Tag.selfClosingInserting(Component.empty())
        }
    }

    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it, this).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    private val parsedPatter = PlaceholderManagerImpl.parse(pattern, this)

    private val widthViewer = ValueViewer<Pair<Style, Int>, Int>()
        .addFunction(
            { (style, codepoint) ->
                when (style.font()) {
                    SPACE_KEY -> codepoint - CENTER_SPACE_CODEPOINT
                    null -> when (codepoint) {
                        TEXT_SPACE_KEY_CODEPOINT -> space
                        else -> data.codepoint[codepoint]?.let { c -> c + 1 }
                    }
                    else -> null
                }
            }
        )

    private val splitOption = SplitOption(
        data.splitWidth,
        space,
        forceSplit
    )

    override fun render(event: UpdateEvent): TickProvider<HudPlayer, PixelComponent> {
        val buildPattern = parsedPatter(event)
        val cond = conditions build event

        val followTarget = followHudPlayer?.build(event)
        val colorApply = colorOverrides(event)

        return tickProvide(tick) build@ { player, _ ->
            var targetHudPlayer = player
            followTarget?.let {
                PlayerManagerImpl.getHudPlayer(it.value(player).toString())?.let { p ->
                    targetHudPlayer = p
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
            }
            if (!cond(targetHudPlayer)) return@build EMPTY_PIXEL_COMPONENT

            var widthComp = EMPTY_WIDTH_COMPONENT

            val compList = buildPattern(targetHudPlayer)
                .parseToComponent()
                .split(splitOption, widthViewer)
            var max = 0
            compList.forEachIndexed { index, comp ->
                if (data.font.lastIndex < index) return@forEachIndexed
                val backgroundKey = data.font[index]
                var finalComp = comp
                finalComp.component.font(backgroundKey.key)
                //TODO replace it to proper background in the future.
                backgroundKey.background?.let {
                    val builder = Component.text().append(it.left.component)
                    var length = 0
                    while (length < comp.width) {
                        builder.append(it.body.component)
                        length += it.body.width
                    }
                    val total = it.left.width + length + it.right.width
                    val minus = -total + (length - comp.width) / 2 + it.left.width - it.x

                    var build = EMPTY_WIDTH_COMPONENT.finalizeFont()
                    if (it.x != 0) build += it.x.toSpaceComponent()
                    build += WidthComponent(builder.append(it.right.component).font(backgroundKey.key), total)
                    if (max < build.width) max = build.width
                    finalComp = build + minus.toSpaceComponent() + finalComp + (-minus - finalComp.width).toSpaceComponent()
                } ?: run {
                    if (max < finalComp.width) max = finalComp.width
                }
                widthComp = widthComp plusWithAlign finalComp
            }
            widthComp.applyColor(colorApply(targetHudPlayer)).toPixelComponent(when (align) {
                LayoutAlign.LEFT -> x
                LayoutAlign.CENTER -> x - max / 2
                LayoutAlign.RIGHT -> x - max
            })
        }
    }

    private infix fun WidthComponent.plusWithAlign(other: WidthComponent) = plusWithAlign(lineAlign, other)
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

    private val miniMessage = MiniMessage.builder()
        .tags(TagResolver.resolver(
            TagResolver.standard(),
            TagResolver.resolver(setOf(
                "image",
                "img"
            )) { queue: ArgumentQueue, _: Context ->
                queue.peek()?.value()?.let { value ->
                    data.imageCodepoint[value]?.let { Tag.selfClosingInserting(Component.text(it.parseChar())) }
                } ?: emptyTag
            },
            TagResolver.resolver(setOf(
                "space",
                "shift"
            )) { queue: ArgumentQueue, _: Context ->
                queue.peek()?.value()?.toIntOrNull()?.let { value ->
                    Tag.selfClosingInserting(value.toSpaceComponent().finalizeFont().component)
                } ?: emptyTag
            }
        ))
        .apply {
            if (color.value() != NamedTextColor.WHITE.value()) postProcessor {
                it.color(color)
            }
        }
        .build()

    private fun String.parseToComponent(): Component {
        var targetString = (if (useLegacyFormat) legacySerializer(this) else Component.text(this))
            .replaceText(TextReplacementConfig.builder()
                .match(allPattern)
                .replacement { r, _ ->
                    miniMessage.deserialize(r.group())
                }
                .build())
        if (!disableNumberFormat) {
            targetString = targetString.replaceText(TextReplacementConfig.builder()
                .match(decimalPattern)
                .replacement { r, _ ->
                    val g = r.group()
                    Component.text(runCatching {
                        numberFormat.format(numberEquation.evaluate(g.toDouble()))
                    }.getOrDefault(g))
                }
                .build())
        }
        return targetString
    }
}