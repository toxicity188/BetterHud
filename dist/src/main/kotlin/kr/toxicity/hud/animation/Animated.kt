package kr.toxicity.hud.animation

class Animated<out T>(
    private val type: AnimationType,
    private val stateOfFrame: IntArray,
    val states: List<T>
) {
    fun at(t: Long): T = states[type.choose(stateOfFrame, t)]

    fun <R> map(mapper: (T) -> R) = Animated(type, stateOfFrame, states.map(mapper))
}
