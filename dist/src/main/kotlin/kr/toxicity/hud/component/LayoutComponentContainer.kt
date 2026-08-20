package kr.toxicity.hud.component

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.layout.enums.LayoutFlow
import kr.toxicity.hud.layout.enums.LayoutOffset
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.toSpaceComponent

class LayoutComponentContainer(
    private val offset: LayoutOffset,
    private val align: LayoutAlign,
    private val flow: LayoutFlow,
    private val flowGap: Int,
    private val max: Int
) {
    private var comp = EMPTY_WIDTH_COMPONENT
    private var flowComp = EMPTY_WIDTH_COMPONENT
    private var flowElementCount = 0

    private fun appendAbsolute(other: PixelComponent) {
        val move = when (align) {
            LayoutAlign.LEFT -> 0
            LayoutAlign.CENTER -> (max - other.component.width) / 2
            LayoutAlign.RIGHT -> max - other.component.width
        }
        comp += (other.pixel + move).toSpaceComponent() + other.component + (-other.pixel - other.component.width - move).toSpaceComponent()
    }

    private fun appendFlow(other: PixelComponent) {
        // In flow mode, we concatenate elements sequentially
        // Add gap before element (except for the first one)
        if (flowElementCount > 0 && flowGap > 0) {
            flowComp += flowGap.toSpaceComponent()
        }
        flowComp += other.component
        flowElementCount++
    }

    fun append(others: List<PixelComponent>): LayoutComponentContainer {
        when (flow) {
            LayoutFlow.NONE -> others.forEach { appendAbsolute(it) }
            LayoutFlow.HORIZONTAL -> others.forEach { appendFlow(it) }
            LayoutFlow.VERTICAL -> {
                // For vertical flow, each element starts at origin (handled by their individual y offsets)
                others.forEach { appendAbsolute(it) }
            }
        }
        return this
    }

    fun build(): WidthComponent {
        return when (flow) {
            LayoutFlow.NONE, LayoutFlow.VERTICAL -> {
                when (offset) {
                    LayoutOffset.LEFT -> 0
                    LayoutOffset.CENTER -> -max / 2
                    LayoutOffset.RIGHT -> -max
                }.toSpaceComponent() + comp
            }
            LayoutFlow.HORIZONTAL -> {
                // For horizontal flow, center the concatenated content based on its actual width
                val totalWidth = flowComp.width
                when (offset) {
                    LayoutOffset.LEFT -> 0
                    LayoutOffset.CENTER -> -totalWidth / 2
                    LayoutOffset.RIGHT -> -totalWidth
                }.toSpaceComponent() + flowComp
            }
        }
    }
}