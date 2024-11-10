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
    private val list = ArrayList<PixelComponent>()

    private fun append(other: PixelComponent) {
        list.add(other)
    }
    fun append(others: List<PixelComponent>): LayoutComponentContainer {
        others.forEach {
            append(it)
        }
        return this
    }

    fun build(): WidthComponent {
        var comp = EMPTY_WIDTH_COMPONENT
        list.forEach {
            val move = when (align) {
                LayoutAlign.LEFT -> 0
                LayoutAlign.CENTER -> (max - it.component.width) / 2
                LayoutAlign.RIGHT -> max - it.component.width
            }
            comp += (it.pixel + move).toSpaceComponent() + it.component + (-it.pixel - it.component.width - move).toSpaceComponent()
        }
        return when (offset) {
            LayoutOffset.LEFT -> 0
            LayoutOffset.CENTER -> -max / 2
            LayoutOffset.RIGHT -> -max
        }.toSpaceComponent() + comp
    }
}