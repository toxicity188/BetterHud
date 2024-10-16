package kr.toxicity.hud.shader

import kr.toxicity.hud.location.GuiLocation

data class HudShader(
    val gui: GuiLocation,
    val renderScale: RenderScale,
    val layer: Int,
    val outline: Boolean,
    val opacity: Double,
    val property: Int,
): Comparable<HudShader> {
    companion object {
        private val comparator = Comparator.comparing { s: HudShader ->
            s.gui
        }.thenComparing { s: HudShader ->
            s.renderScale
        }.thenComparing { s: HudShader ->
            s.layer
        }.thenComparing { s: HudShader ->
            s.outline
        }.thenComparing { s: HudShader ->
            s.opacity
        }.thenComparing { s: HudShader ->
            s.property
        }
    }

    override fun compareTo(other: HudShader): Int {
        return comparator.compare(this, other)
    }
}