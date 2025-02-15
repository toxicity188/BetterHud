package kr.toxicity.hud.element

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.ImageTextScale
import kr.toxicity.hud.text.TextScale

class TextElement(
    override val id: String,
    val textScale: Int?,
    val array: List<HudTextArray>,
    val charWidth: Map<Int, TextScale>,
    val imageTextScale: Map<Int, ImageTextScale>,
    yamlObject: YamlObject
) : HudElement, ConditionSource by ConditionSource.Impl(yamlObject).memoize()