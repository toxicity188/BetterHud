package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.memoizeBuilder
import kr.toxicity.hud.util.toColorOverrides
import kr.toxicity.hud.util.toConditions

interface ConditionSource {
    val conditions: ConditionBuilder
    val colorOverrides: ColorOverride.Builder

    operator fun plus(other: ConditionSource) = Impl(
        colorOverrides + other.colorOverrides,
        conditions and other.conditions
    )

    fun memoize() = Impl(
        colorOverrides.memoizeBuilder(null),
        conditions.memoize()
    )

    class Impl(
        override val colorOverrides: ColorOverride.Builder,
        override val conditions: ConditionBuilder,
    ) : ConditionSource {
        constructor(yamlObject: YamlObject, source: PlaceholderSource): this(
            yamlObject.toColorOverrides(source),
            yamlObject.toConditions(source),
        )
        constructor(yamlObject: YamlObject): this(
            yamlObject,
            PlaceholderSource.Impl(yamlObject)
        )
    }
}