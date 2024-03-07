package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.TriggerManager
import kr.toxicity.hud.api.trgger.HudBukkitEventTrigger
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.UUID
import java.util.function.Function

object TriggerManagerImpl: MythicHudManager, TriggerManager {
    private val listener = object : Listener {}

    private val map = mutableMapOf<String, HudTrigger<*>>(
        "attack" to object : HudBukkitEventTrigger<EntityDamageByEntityEvent> {
            override fun getEventClass(): Class<EntityDamageByEntityEvent> {
                return EntityDamageByEntityEvent::class.java
            }

            override fun getKeyMapper(): Function<in EntityDamageByEntityEvent, UUID> {
                return Function { e ->
                    e.entity.uniqueId
                }
            }

            override fun getValueMapper(): Function<in EntityDamageByEntityEvent, UUID?> {
                return Function { e ->
                    val attacker = e.damager
                    if (attacker is Player) attacker.uniqueId else null
                }
            }
        }
    )
    private val actionMap = HashMap<String, MutableList<(UpdateEvent, UUID) -> Boolean>>()

    override fun start() {

    }

    override fun addTrigger(name: String, trigger: HudTrigger<*>) {
        map[name] = trigger
    }

    fun addTask(name: String, task: (UpdateEvent, UUID) -> Boolean) {
        if (map.containsKey(name)) {
            actionMap.getOrPut(name) {
                ArrayList()
            }.add(task)
        } else {
            throw RuntimeException("Unable to find this trigger: $name")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun reload(resource: GlobalResource) {
        HandlerList.unregisterAll(listener)
        actionMap.clear()
        map.forEach {
            when (val type = it.value) {
                is HudBukkitEventTrigger<out Event> -> {
                    Bukkit.getPluginManager().registerEvent(type.eventClass, listener, EventPriority.NORMAL, { _, e ->
                        if (type.eventClass.isAssignableFrom(e.javaClass)) {
                            actionMap[it.key]?.let { action ->
                                val cast = type.eventClass.cast(e)
                                (type.valueMapper as Function<Event, UUID?>).apply(cast)?.let { uuid ->
                                    val wrapper = BukkitEventUpdateEvent(cast, (type.keyMapper as Function<Event, UUID>).apply(cast))
                                    action.removeIf { act ->
                                        !act(wrapper, uuid)
                                    }
                                }
                            }
                        }
                    }, PLUGIN)
                }
            }
        }
    }
    override fun end() {
    }
}