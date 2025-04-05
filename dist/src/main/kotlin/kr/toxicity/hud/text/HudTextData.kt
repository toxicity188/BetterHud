package kr.toxicity.hud.text

import kr.toxicity.hud.util.IntEntryMap

class HudTextData(
    val font: List<BackgroundKey>,
    val codepoint: IntEntryMap,
    val imageCodepoint: Map<String, Int>,
    val splitWidth: Int
)