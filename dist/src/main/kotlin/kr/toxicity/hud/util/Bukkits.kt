package kr.toxicity.hud.util

import kr.toxicity.hud.api.update.BukkitEventUpdateEvent
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import java.util.UUID

fun Event.call(): Boolean {
    Bukkit.getPluginManager().callEvent(this)
    return if (this is Cancellable) !isCancelled else true
}

fun Event.toUpdateEvent(key: Any = UUID.randomUUID()) = BukkitEventUpdateEvent(this, key)