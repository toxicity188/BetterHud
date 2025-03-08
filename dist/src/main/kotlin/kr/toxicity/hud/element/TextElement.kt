package kr.toxicity.hud.element

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.ImageTextScale
import kr.toxicity.hud.text.TextScale
import kr.toxicity.hud.util.IntKeyMap

class TextElement(
    override val id: String,
    val textScale: Int?,
    val array: List<HudTextArray>,
    val charWidth: IntKeyMap<TextScale>,
    val imageTextScale: IntKeyMap<ImageTextScale>,
    yamlObject: YamlObject
) : HudElement, ConditionSource by ConditionSource.Impl(yamlObject)