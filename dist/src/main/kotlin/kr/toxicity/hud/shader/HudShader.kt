package kr.toxicity.hud.shader

data class HudShader(
    val gui: GuiLocation,
    val layer: Int,
    val outline: Boolean
): Comparable<HudShader> {
    companion object {
        private val comparator = Comparator.comparing { s: HudShader ->
            s.gui.x
        }.thenComparing { s: HudShader ->
            s.gui.y
        }.thenComparing { s: HudShader ->
            s.layer
        }.thenComparing { s: HudShader ->
            s.outline
        }
    }

    override fun compareTo(other: HudShader): Int {
        return comparator.compare(this, other)
    }
}