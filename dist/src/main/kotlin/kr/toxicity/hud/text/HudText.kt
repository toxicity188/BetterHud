package kr.toxicity.hud.text

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.placeholder.ConditionBuilder

class HudText(
    override val path: String,
    val name: String,
    val array: List<HudTextArray>,
    val charWidth: Map<Int, CharWidth>,
    val imageCharWidth: Map<Int, ImageCharWidth>,
    val conditions: ConditionBuilder
) : HudConfiguration