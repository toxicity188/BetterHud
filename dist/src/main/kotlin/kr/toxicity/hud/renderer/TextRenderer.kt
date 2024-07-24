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
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.roundToInt

class TextRenderer(
    private val widthMap: Map<Int, Int>,
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

    private val useLegacyFormat: Boolean,
    private val legacySerializer: LegacyComponentSerializer,
    private val condition: ConditionBuilder
) {
    companion object {
        private const val SPACE_POINT = ' '.code
        private val decimalPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val allPattern = Pattern.compile(".+")
        private val imagePattern = Pattern.compile("<image:(?<name>([a-zA-Z]+))>")
    }

    private val followPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    private val parsedPatter = PlaceholderManagerImpl.parse(pattern)

    fun getText(reason: UpdateEvent): (HudPlayer) -> PixelComponent {
        val buildPattern = parsedPatter(reason)
        val cond = condition.build(reason)

        val followTarget = followPlayer?.build(reason)

        return build@ { player ->
            var targetPlayer = player
            followTarget?.let {
                targetPlayer = Bukkit.getPlayer(it.value(player).toString())?.let { p ->
                    PlayerManagerImpl.getHudPlayer(p.uniqueId)
                } ?: return@build EMPTY_PIXEL_COMPONENT
            }
            if (!cond(targetPlayer)) return@build EMPTY_PIXEL_COMPONENT
            var width = 0
            val targetString = (if (useLegacyFormat) legacySerializer.deserialize(buildPattern(targetPlayer)) else Component.text(buildPattern(targetPlayer)))
                .color(defaultColor)
                .replaceText(TextReplacementConfig.builder()
                    .match(imagePattern)
                    .replacement { r, _ ->
                        data.images[r.group(1)]?.let {
                            width += it.width + 1
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
            fun applyDecoration(component: Component): Component {
                var ret = component
                if (ret is TextComponent) {
                    var group = ret.content()
                    if (!disableNumberFormat) {
                        group = decimalPattern.matcher(group).replaceAll {
                            val g = it.group()
                            runCatching {
                                numberPattern.format(numberEquation.evaluate(g.toDouble()))
                            }.getOrDefault(g)
                        }
                        ret = ret.content(group)
                    }
                    val codepoint = group.codePoints().toArray()
                    val count = codepoint.count {
                        it != SPACE_POINT
                    }
                    if (component.hasDecoration(TextDecoration.BOLD)) width += count
                    if (component.hasDecoration(TextDecoration.ITALIC)) width += count
                    if (component.font() == null) {
                        width += codepoint.sumOf {
                            if (it != SPACE_POINT) ((widthMap[it] ?: 0).toDouble() * scale).roundToInt() + 1 else 4
                        }
                        ret = ret.font(data.word)
                    }
                }
                return ret.children(ret.children().map {
                    applyDecoration(it)
                })
            }

            var comp = WidthComponent(Component.text().append(applyDecoration(targetString)), width)

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