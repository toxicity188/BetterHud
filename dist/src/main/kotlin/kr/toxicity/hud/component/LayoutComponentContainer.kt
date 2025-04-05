package kr.toxicity.hud.component

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.layout.enums.LayoutOffset
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.toSpaceComponent

class LayoutComponentContainer(
    private val offset: LayoutOffset,
    private val align: LayoutAlign,
    private val max: Int
) {
    private var comp = EMPTY_WIDTH_COMPONENT

    private fun append(other: PixelComponent) {
        val move = when (align) {
            LayoutAlign.LEFT -> 0
            LayoutAlign.CENTER -> (max - other.component.width) / 2
            LayoutAlign.RIGHT -> max - other.component.width
        }
        comp += (other.pixel + move).toSpaceComponent() + other.component + (-other.pixel - other.component.width - move).toSpaceComponent()
    }

    fun append(others: List<PixelComponent>): LayoutComponentContainer {
        others.forEach {
            append(it)
        }
        return this
    }

    fun build(): WidthComponent {
        return when (offset) {
            LayoutOffset.LEFT -> 0
            LayoutOffset.CENTER -> -max / 2
            LayoutOffset.RIGHT -> -max
        }.toSpaceComponent() + comp
    }
}