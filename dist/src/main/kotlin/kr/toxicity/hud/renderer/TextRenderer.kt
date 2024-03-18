package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import java.text.DecimalFormat
import java.util.LinkedList
import java.util.regex.Pattern
import kotlin.math.ceil

class TextRenderer(
    private val widthMap: Map<Char, Int>,
    private val style: Style,
    private val pattern: String,
    private val align: TextLayout.Align,
    private val scale: Double,
    private val x: Int,

    space: Int,

    private val numberEquation: TEquation,
    private val numberPattern: DecimalFormat,

    private val condition: ConditionBuilder
) {
    companion object {
        private val textPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val spaceComponent = 4.toSpaceComponent()
    }

    private val sComponent = space.toSpaceComponent()
    fun getText(reason: UpdateEvent): (HudPlayer) -> PixelComponent {
        val parse = PlaceholderManagerImpl.parse(reason, pattern)
        val cond = condition.build(reason)
        return build@ { player ->
            var comp = EMPTY_WIDTH_COMPONENT
            var original = if (cond(player)) parse(player) else ""
            if (original == "") return@build EMPTY_PIXEL_COMPONENT
            val matcher = textPattern.matcher(original)
            val number = LinkedList<String>()
            while (matcher.find()) {
                number.add(numberPattern.format(numberEquation.evaluate(matcher.group().toDouble())))
            }
            if (number.isNotEmpty()) {
                val sb = StringBuilder()
                original.split(textPattern).forEach {
                    sb.append(it)
                    number.poll()?.let { n ->
                        sb.append(n)
                    }
                }
                original = sb.toString()
            }
            original.forEachIndexed { index, char ->
                if (char == ' ') {
                    comp += spaceComponent
                } else {
                    widthMap[char]?.let { width ->
                        comp += WidthComponent(Component.text(char).style(style), ceil(width.toDouble() * scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                    }
                }
                if (index < original.lastIndex) comp += sComponent
            }
            comp.toPixelComponent(when (align) {
                TextLayout.Align.LEFT -> x
                TextLayout.Align.CENTER -> x - comp.width / 2
                TextLayout.Align.RIGHT -> x - comp.width
            })
        }
    }
}