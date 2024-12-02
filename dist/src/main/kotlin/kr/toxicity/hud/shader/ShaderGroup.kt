package kr.toxicity.hud.shader

import kr.toxicity.hud.layout.HudLayout

data class ShaderGroup(
    val shader: HudShader,
    override val name: String,
    val ascent: Int
) : HudLayout.Identifier