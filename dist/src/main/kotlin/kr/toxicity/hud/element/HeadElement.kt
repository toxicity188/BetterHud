package kr.toxicity.hud.element

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.ConditionSource

class HeadElement(
    override val id: String,
    yaml: YamlObject
) : HudElement, ConditionSource by ConditionSource.Impl(yaml).memoize() {
    val pixel = yaml.getAsInt("pixel", 1).coerceAtLeast(1)
}