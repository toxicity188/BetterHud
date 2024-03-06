package kr.toxicity.hud.component

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.NEGATIVE_ONE_SPACE_COMPONENT
import kr.toxicity.hud.util.NEW_LAYER
import kr.toxicity.hud.util.toSpaceComponent
import kotlin.math.abs
import kotlin.math.max

class LayoutComponentContainer {

    private var widthComponent = EMPTY_WIDTH_COMPONENT
    private var max = 0

    private fun append(other: WidthComponent): LayoutComponentContainer {
        widthComponent += other + (-other.width).toSpaceComponent()
        if (max < other.width) max = other.width
        return this
    }
    fun append(others: List<WidthComponent>): LayoutComponentContainer {
        others.forEach { c ->
            append(c)
        }
        return this
    }

    fun build() = widthComponent + max.toSpaceComponent()

    operator fun plus(other: LayoutComponentContainer) = LayoutComponentContainer().also {
        it.widthComponent = widthComponent + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER + other.widthComponent
        it.max = max(max, other.max)
    }
}