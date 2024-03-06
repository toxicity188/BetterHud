package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.NEGATIVE_ONE_SPACE_COMPONENT
import kr.toxicity.hud.util.NEW_LAYER
import kr.toxicity.hud.util.toSpaceComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import kotlin.math.ceil

class TextRenderer(
    private val widthMap: Map<Char, Int>,
    private val style: Style,
    private val pattern: String,
    private val align: TextLayout.Align,
    private val scale: Double,
    x: Int,
    space: Int,
    private val condition: (HudPlayer) -> Boolean
) {
    private val sComponent = space.toSpaceComponent()
    private val xComponent = x.toSpaceComponent()
    companion object {
        private val spaceComponent = 4.toSpaceComponent()
    }
    fun getText(player: HudPlayer): WidthComponent {
        var comp = EMPTY_WIDTH_COMPONENT
        val original = if (condition(player)) PlaceholderManagerImpl.parse(player, pattern) else ""
        if (original == "") return comp
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
        when (align) {
            TextLayout.Align.LEFT -> {
                comp = (-comp.width).toSpaceComponent() + comp
            }
            TextLayout.Align.CENTER -> {
                comp = (-comp.width / 2).toSpaceComponent() + comp
            }
            else -> {}
        }
        return xComponent + comp
    }
}