package kr.toxicity.hud.bootstrap.bukkit.module.bukkit

import kr.toxicity.hud.api.bukkit.event.UpdateItemEvent
import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.module.BukkitModule
import kr.toxicity.hud.bootstrap.bukkit.util.call
import kr.toxicity.hud.bootstrap.bukkit.util.createBukkitTrigger
import kr.toxicity.hud.bootstrap.bukkit.util.unwrap
import kr.toxicity.hud.util.BOOTSTRAP
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.Plugin
import java.util.function.Function

class BukkitItemModule : BukkitModule {
    init {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun drop(e: PlayerDropItemEvent) {
                UpdateItemEvent(e.player, e.itemDrop.itemStack, e).call()
            }
            @EventHandler
            fun pickup(e: EntityPickupItemEvent) {
                val entity = e.entity
                if (entity is Player) UpdateItemEvent(
                    entity,
                    e.item.itemStack,
                    e
                ).call()
            }
        }, BOOTSTRAP as Plugin)
    }
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
            "update" to { c ->
                c.get("type")?.asString()?.let {
                    val predicate: (UpdateItemEvent) -> Boolean = when (it) {
                        "drop" -> { e -> e.original is PlayerDropItemEvent }
                        "pickup" -> { e -> e.original is EntityPickupItemEvent }
                        else -> throw RuntimeException("this type doesn't exist: $it")
                    }
                    createBukkitTrigger(UpdateItemEvent::class.java, { e ->
                        if (predicate(e)) e.player.uniqueId else null
                    }, { e ->
                        e.itemStack
                    })
                } ?: createBukkitTrigger(UpdateItemEvent::class.java, {
                    it.player.uniqueId
                }, {
                    it.itemStack
                })
            },
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "amount" to HudPlaceholder.of { _, u ->
                u.unwrap { e: UpdateItemEvent ->
                    Function {
                        e.itemStack.amount
                    }
                }
            },
            "custom_model_data" to HudPlaceholder.of { _, u ->
                u.unwrap { e: UpdateItemEvent ->
                    Function {
                        e.itemMeta.customModelData
                    }
                }
            },
            "durability" to HudPlaceholder.of { _, u ->
                u.unwrap { e: UpdateItemEvent ->
                    Function {
                        (e.itemMeta as? Damageable)?.damage ?: -1
                    }
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "display_name" to HudPlaceholder.of { _, u ->
                u.unwrap { e: UpdateItemEvent ->
                    Function {
                        e.itemMeta.displayName
                    }
                }
            },
            "type" to HudPlaceholder.of { _, u ->
                u.unwrap { e: UpdateItemEvent ->
                    Function {
                        e.itemStack.type.name
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()
}