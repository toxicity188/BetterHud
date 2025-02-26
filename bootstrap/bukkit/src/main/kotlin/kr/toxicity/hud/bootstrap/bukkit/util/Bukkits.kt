package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.api.bukkit.BukkitBootstrap
import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.ifNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.function.BiConsumer

val HudPlayer.bukkitPlayer
    get() = handle() as Player

val Player.hudPlayer
    get() = PlayerManagerImpl.getHudPlayer(uniqueId).ifNull { "Unable to find this player: $name" }

fun Event.call(): Boolean {
    Bukkit.getPluginManager().callEvent(this)
    return if (this is Cancellable) !isCancelled else true
}

fun Event.toUpdateEvent(key: Any = UUID.randomUUID()) = BukkitEventUpdateEvent(this, key)

inline fun <reified T : Event, R : Any> UpdateEvent.unwrap(block: (T) -> R): R {
    val evt = source()
    return if (evt is BukkitEventUpdateEvent) {
        val e = evt.event
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
        override fun getKey(t: T): Any = keyMapper(t)
        override fun registerEvent(eventConsumer: BiConsumer<UUID, UpdateEvent>) {

            Bukkit.getPluginManager().registerEvent(clazz, (BOOTSTRAP as BukkitBootstrap).triggerListener(), EventPriority.MONITOR, { _, e ->
                if (clazz.isAssignableFrom(e.javaClass)) {
                    val cast = clazz.cast(e)
                    valueMapper(cast)?.let { uuid ->
                        val wrapper = BukkitEventUpdateEvent(
                            cast,
                            keyMapper(cast)
                        )
                        eventConsumer.accept(uuid, wrapper)
                    }
                }
            }, BOOTSTRAP as Plugin, true)
        }
    }
}