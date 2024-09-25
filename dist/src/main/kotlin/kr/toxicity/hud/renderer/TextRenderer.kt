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
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.text.DecimalFormat
import java.util.regex.Pattern

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
    private val legacySerializer: LegacyComponentSerializer,
    private val space: Int,
    private val condition: ConditionBuilder
) {
    companion object {
        private const val SPACE_POINT = ' '.code
        private val decimalPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val allPattern = Pattern.compile(".+")
        private val imagePattern = Pattern.compile("<image:(?<name>([a-zA-Z]+))>")
    }

    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    private val parsedPatter = PlaceholderManagerImpl.parse(pattern)

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
            var width = 0
            val patternResult = buildPattern(targetHudPlayer)
            var targetString = (if (useLegacyFormat) legacySerializer.deserialize(patternResult) else Component.text(patternResult))
                .color(defaultColor)
                .replaceText(TextReplacementConfig.builder()
                    .match(imagePattern)
                    .replacement { r, _ ->
                        data.images[r.group(1)]?.let {
                            width += it.width
                            it.component.build()
                        } ?: Component.empty()
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
                    var add = 0
                    if (bold) add++
                    if (italic) add++
                    if (space != 0) {
                        ret = ret.content(buildString {
                            codepoint.forEachIndexed { index, i ->
                                appendCodePoint(i)
                                width += if (i != SPACE_POINT) widthMap[i]?.let { width ->
                                    width.scaledWidth(scale) + add + 1
                                } ?: 0 else (4 + add)
                                if (index < codepoint.lastIndex) {
                                    width += space + add
                                    appendCodePoint(TEXT_SPACE_KEY_CODEPOINT)
                                }
                            }
                        })
                    } else {
                        width += codepoint.sumOf {
                            if (it != SPACE_POINT) widthMap[it]?.let { width ->
                                width.scaledWidth(scale) + add + 1
                            } ?: 0 else (4 + add)
                        }
                    }
                    ret = ret.font(data.word)
                }
                return ret.children(ret.children().map {
                    applyDecoration(
                        it,
                        hasDecoration(bold, it.decoration(TextDecoration.BOLD)),
                        hasDecoration(italic, it.decoration(TextDecoration.ITALIC)),
                    )
                })
            }
            val finalComp = Component.text().append(applyDecoration(
                targetString,
                hasDecoration(false, targetString.decoration(TextDecoration.BOLD)),
                hasDecoration(false, targetString.decoration(TextDecoration.ITALIC))
            ))
            var comp = WidthComponent(finalComp, width)

            data.background?.let {
                val builder = Component.text().append(it.left.component)
                var length = 0
                while (length < comp.width) {
                    builder.append(it.body.component)
                    length += it.body.width
                }
                val total = it.left.width + length + it.right.width
                val minus = -total + (length - comp.width) / 2 + it.left.width - it.x
                comp = it.x.toSpaceComponent() + WidthComponent(builder.append(it.right.component), total) + minus.toSpaceComponent() + comp
            }
            comp.toPixelComponent(when (align) {
                LayoutAlign.LEFT -> x
                LayoutAlign.CENTER -> x - comp.width / 2
                LayoutAlign.RIGHT -> x - comp.width
            })
        }
    }
}