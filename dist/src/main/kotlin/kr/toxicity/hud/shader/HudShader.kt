package kr.toxicity.hud.shader

data class HudShader(
    val gui: GuiLocation,
    val renderScale: RenderScale,
    val layer: Int,
    val outline: Boolean,
    val opacity: Double
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
        }
    }

    override fun compareTo(other: HudShader): Int {
        return comparator.compare(this, other)
    }
}