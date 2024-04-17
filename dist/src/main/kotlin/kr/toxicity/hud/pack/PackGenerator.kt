package kr.toxicity.hud.pack

import kr.toxicity.hud.util.forEachAsync

object PackGenerator {
    private val tasks = ArrayList<() -> Unit>()

    fun generate(callback: () -> Unit) {
        tasks.forEachAsync({ _, t ->
            t()
        }) {
            callback()
            tasks.clear()
        }
    }

    fun addTask(block: () -> Unit) {
        tasks.add(block)
    }
}