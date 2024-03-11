package kr.toxicity.hud.module.bukkit

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trgger.HudBukkitEventTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.module.BukkitModule
import kr.toxicity.hud.util.createBukkitTrigger
import kr.toxicity.hud.util.unwrap
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*
import java.util.function.Function

class BukkitEntityModule: BukkitModule {
    override val triggers: Map<String, (ConfigurationSection) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
            "attack" to {
                createBukkitTrigger(EntityDamageByEntityEvent::class.java, {
                    val attacker = it.damager
                    if (attacker is Player) attacker.uniqueId else null
                }, {
                    it.entity.uniqueId
                })
            },
            "damage" to {
                createBukkitTrigger(EntityDamageByEntityEvent::class.java, {
                    val victim = it.entity
                    if (victim is Player) victim.uniqueId else null
                }, {
                    it.entity.uniqueId
                })
            },
            "dead" to {
                createBukkitTrigger(PlayerDeathEvent::class.java, {
                    it.entity.uniqueId
                }, {
                    it.entity.killer?.uniqueId ?: UUID.randomUUID()
                })
            },
            "kill" to {
                createBukkitTrigger(EntityDeathEvent::class.java, {
                    it.entity.killer?.uniqueId
                }, {
                    it.entity.uniqueId
                })
            },
        )
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to {
                { event ->
                    event.unwrap ref@ { target: EntityEvent ->
                        val entity = target.entity as? LivingEntity ?: return@ref HudListener.ZERO
                        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value?.let { maxHealth ->
                            HudListener {
                                entity.health / maxHealth
                            }
                        } ?: HudListener.ZERO
                    }
                }
            }
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        (e.entity as? LivingEntity)?.health ?: 0.0
                    }
                }
            },
            "max_health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        (e.entity as? LivingEntity)?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0.0
                    }
                }
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        e.entity.name
                    }
                }
            },
            "type" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        e.entity.type.name
                    }
                }
            },
            "custom_name" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        e.entity.customName ?: e.entity.name
                    }
                }
            },
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "dead" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        e.entity.isDead
                    }
                }
            },
        )

}