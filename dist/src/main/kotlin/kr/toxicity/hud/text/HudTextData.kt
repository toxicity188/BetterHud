package kr.toxicity.hud.text

class HudTextData(
    val font: List<BackgroundKey>,
    val codepoint: Map<Int, Int>,
    val imageCodepoint: Map<String, Int>,
    val splitWidth: Int
)