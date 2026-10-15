package kr.toxicity.hud.shader

import kr.toxicity.hud.util.HUD_MAX_BIT

data class ShaderBudget(val used: Int, val max: Int = MAX_SHADER_ID) {
    companion object {
        const val MAX_SHADER_ID = (1 shl HUD_MAX_BIT) - 1
    }

    val exceeded get() = used > max

    fun message() = if (exceeded) {
        "Shader ids exceeded: $used / $max. Elements beyond the budget will render incorrectly; reduce distinct opacity, layer or render-scale states."
    } else "Shader ids used: $used / $max"
}
