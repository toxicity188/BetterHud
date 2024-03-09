package kr.toxicity.hud.component

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.toSpaceComponent

class LayoutComponentContainer {
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
        var max = 0
        list.forEach {
            if (max < it.component.width) max = it.component.width
            comp += it.pixel.toSpaceComponent() + it.component + (-it.pixel - it.component.width).toSpaceComponent()
        }
        return (-max / 2).toSpaceComponent() + comp
    }
}