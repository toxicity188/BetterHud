package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.update.UpdateEvent

interface PlaceholderBuilder<T : Any> {
    val clazz: Class<out T>
    fun build(reason: UpdateEvent): Placeholder<T>
}