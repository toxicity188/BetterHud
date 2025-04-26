package kr.toxicity.hud.player

import kr.toxicity.hud.api.scheduler.HudTask

class HudPlayerTask(
    private val creator: () -> HudTask?
) : HudTask {
    private var initialTask = creator()
        set(value) {
            field?.cancel()
            field = value
        }

    @Synchronized
    fun restart() {
        initialTask = creator()
    }

    @Synchronized
    override fun cancel() {
        initialTask = null
    }

    override fun isCancelled(): Boolean = initialTask?.isCancelled != false
}