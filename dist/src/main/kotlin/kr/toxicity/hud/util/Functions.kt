package kr.toxicity.hud.util

import kr.toxicity.hud.api.trgger.HudBukkitEventTrigger
import kr.toxicity.hud.api.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.LayoutAlign
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent
import java.util.UUID

fun <T> T?.ifNull(message: String): T & Any {
    return this ?: throw RuntimeException(message)
}

fun String.toEquation() = TEquation(this)

fun String?.toLayoutAlign(): LayoutAlign = if (this != null) LayoutAlign.valueOf(uppercase()) else LayoutAlign.LEFT

inline fun <reified T : Event, R : Any> UpdateEvent.unwrap(block: (T) -> R): R {
    return if (this is BukkitEventUpdateEvent) {
        val e = event
        if (e is T) block(e)
        else throw RuntimeException("Unsupported event found: ${e.javaClass.simpleName}")
    } else throw RuntimeException("Unsupported update found: ${javaClass.simpleName}")
}

fun <T : Event> createBukkitTrigger(
    clazz: Class<T>,
    valueMapper: (T) -> UUID? = { if (it is PlayerEvent) it.player.uniqueId else null },
    keyMapper: (T) -> Any = { UUID.randomUUID() }
): HudBukkitEventTrigger<T> {
    return object : HudBukkitEventTrigger<T> {
        override fun getEventClass(): Class<T> = clazz
        override fun getValue(t: T): UUID? = valueMapper(t)
        override fun getKey(t: T): Any = keyMapper(t)
    }
}