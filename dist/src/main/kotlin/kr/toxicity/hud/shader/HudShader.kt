package kr.toxicity.hud.shader

data class HudShader(
    val gui: GuiLocation,
    val layer: Int,
    val outline: Boolean
) {
    data class GuiLocation(val x: Double, val y: Double)
}