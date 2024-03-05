package kr.toxicity.hud.layout

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.text.HudText
import net.kyori.adventure.text.format.TextColor

class TextLayout(
    private val pattern: String,
    val text: HudText,
    val x: Int,
    val y: Int,
    val scale: Double,
    val space: Int,
    val align: Align,
    val color: TextColor,
    val outline: Boolean,
    val layout: Int,
    val conditions: (HudPlayer) -> Boolean
) {
    enum class Align {
        LEFT,
        CENTER,
        RIGHT
    }

    fun getText(player: HudPlayer): String {
        return if (text.conditions(player) && conditions(player)) PlaceholderManagerImpl.parse(player, pattern) else ""
    }
}