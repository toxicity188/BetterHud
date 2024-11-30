package kr.toxicity.hud.element

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.ConditionSource
import kr.toxicity.hud.text.CharWidth
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.ImageCharWidth

class TextElement(
    override val path: String,
    val name: String,
    val array: List<HudTextArray>,
    val charWidth: Map<Int, CharWidth>,
    val imageCharWidth: Map<Int, ImageCharWidth>,
    yamlObject: YamlObject
) : HudElement, ConditionSource by ConditionSource.Impl(yamlObject)