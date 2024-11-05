package kr.toxicity.hud.text

import net.kyori.adventure.key.Key

class HudTextData(
    val font: List<Key>,
    val imageCodepoint: Map<String, Int>,
    val splitWidth: Int
)