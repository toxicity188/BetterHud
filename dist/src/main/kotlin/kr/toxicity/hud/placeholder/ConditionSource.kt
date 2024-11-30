package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.toColorOverrides
import kr.toxicity.hud.util.toConditions

interface ConditionSource {
    val conditions: ConditionBuilder
    val colorOverrides: ColorOverride.Builder

    operator fun plus(other: ConditionSource) = Impl(
        colorOverrides + other.colorOverrides,
        conditions and other.conditions
    )

    class Impl(
        override val colorOverrides: ColorOverride.Builder,
        override val conditions: ConditionBuilder
    ) : ConditionSource {
        constructor(yamlObject: YamlObject): this(
            yamlObject.toColorOverrides(),
            yamlObject.toConditions()
        )
        constructor(parent: ConditionSource, yamlObject: YamlObject): this(
            parent.colorOverrides + yamlObject.toColorOverrides(),
            parent.conditions and yamlObject.toConditions()
        )
    }
}