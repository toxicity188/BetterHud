package kr.toxicity.hud.text

import kr.toxicity.hud.api.player.HudPlayer

class HudText(
    val name: String,
    val fontName: String,
    val height: Int,
    val array: List<HudTextArray>,
    val charWidth: Map<Char, Int>,
    val conditions: (HudPlayer) -> Boolean
)