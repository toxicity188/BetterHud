package kr.toxicity.hud.shader

data class HotBarShader(
    val gui: Pair<Double, Double>,
    val pixel: Pair<Int, Int>
) {
    companion object {
        val empty = HotBarShader(50.0 to 50.0, 0 to 0)
    }
}