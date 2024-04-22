package kr.toxicity.hud.util

class TaskIndex(val max: Int) {
    @Volatile
    var current = 0
    override fun toString(): String {
        return "$current == $max"
    }
}