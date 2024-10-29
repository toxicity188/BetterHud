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
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor

class TextRenderer(
    private val widthMap: Map<Int, CharWidth>,
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
        private const val SPACE_POINT = ' '.code
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
                if (it.first.font() == null) {
                    if (it.second == SPACE_POINT) 4 + it.first.addNumber()
                    else widthMap[it.second]?.scaledWidth(scale)?.let { w ->
                        w + 1 + it.first.addNumber()
                    }
                } else null
            },
            {
                if (it.first.font() == null && it.second == TEXT_SPACE_KEY_CODEPOINT) space + it.first.addNumber() else null
            },
            data.images.map {
                val comp = it.value
                comp.component.content().codePointAt(0) to comp.width + 1
            }.toMap().let {
                { pair: Pair<Style, Int> ->
                    it[pair.second]?.let {
                        it + 1 + pair.first.addNumber()
                    }
                }
            },
            {
                when (it.first.font()) {
                    SPACE_KEY -> it.second - CURRENT_CENTER_SPACE_CODEPOINT + it.first.addNumber()
                    LEGACY_SPACE_KEY -> it.second - LEGACY_CENTER_SPACE_CODEPOINT + it.first.addNumber()
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

            buildPattern(targetHudPlayer)
                .parseToComponent()
                .split(data.splitWidth, widthViewer)
                .forEachIndexed { index, comp ->
                    if (data.words.lastIndex < index) return@forEachIndexed
                    if (comp !is TextComponent) return@forEachIndexed
                    fun Component.search(): Int {
                        var i = 0
                        if (this is TextComponent) {
                            val style = style()
                            for (codePoint in content().codePoints()) {
                                widthViewer(style to codePoint)?.let { w ->
                                    i += w
                                }
                            }
                        }
                        return i + children().sumOf { children ->
                            children.search()
                        }
                    }
                    val newComp = WidthComponent(comp.toBuilder().font(data.words[index]), comp.search())
                    widthComp = if (widthComp.width == 0) newComp else widthComp plusWithAlign newComp
                }
            data.background?.let {
                it.build(data.words.size, widthComp.width)

            }


//            data.background?.let {
//                val builder = Component.text().append(it.left.component)
//                var length = 0
//                while (length < comp.width) {
//                    builder.append(it.body.component)
//                    length += it.body.width
//                }
//                val total = it.left.width + length + it.right.width
//                val minus = -total + (length - comp.width) / 2 + it.left.width - it.x
//                comp = it.x.toSpaceComponent() + WidthComponent(builder.append(it.right.component), total) + minus.toSpaceComponent() + comp
//            }
            widthComp.toPixelComponent(when (align) {
                LayoutAlign.LEFT -> x
                LayoutAlign.CENTER -> x - widthComp.width / 2
                LayoutAlign.RIGHT -> x - widthComp.width
            })
        }
    }

    private infix fun WidthComponent.plusWithAlign(other: WidthComponent) = when (align) {
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

    private fun Style.addNumber(): Int {
        var i = 0
        if (hasDecoration(TextDecoration.BOLD)) i++
        if (hasDecoration(TextDecoration.ITALIC)) i++
        return i
    }

    private fun String.parseToComponent(): Component {
        var targetString = (if (useLegacyFormat) legacySerializer(this) else Component.text(this))
            .color(defaultColor)
            .replaceText(TextReplacementConfig.builder()
                .match(imagePattern)
                .replacement { r, _ ->
                    when (r.group(1)) {
                        "image" -> data.images[r.group(3)]?.component?.build() ?: Component.empty()
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
        fun hasDecoration(parent: Boolean, state: TextDecoration.State) = when (state) {
            TextDecoration.State.TRUE -> true
            TextDecoration.State.NOT_SET -> parent
            TextDecoration.State.FALSE -> false
        }
        fun applyDecoration(component: Component, bold: Boolean, italic: Boolean): Component {
            var ret = component
            if (ret is TextComponent && ret.font() == null) {
                val codepoint = ret.content().codePoints().toArray()
                if (space != 0) {
                    ret = ret.content(buildString {
                        codepoint.forEachIndexed { index, i ->
                            appendCodePoint(i)
                            if (index < codepoint.lastIndex) {
                                appendCodePoint(TEXT_SPACE_KEY_CODEPOINT)
                            }
                        }
                    })
                }
            }
            return ret.children(ret.children().map {
                applyDecoration(
                    it,
                    hasDecoration(bold, it.decoration(TextDecoration.BOLD)),
                    hasDecoration(italic, it.decoration(TextDecoration.ITALIC)),
                )
            })
        }
        return applyDecoration(
            targetString,
            hasDecoration(false, targetString.decoration(TextDecoration.BOLD)),
            hasDecoration(false, targetString.decoration(TextDecoration.ITALIC))
        )
    }
}