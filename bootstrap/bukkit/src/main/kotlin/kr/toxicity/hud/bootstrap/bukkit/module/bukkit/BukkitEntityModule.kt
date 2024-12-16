package kr.toxicity.hud.bootstrap.bukkit.module.bukkit

import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.module.BukkitModule
import kr.toxicity.hud.bootstrap.bukkit.util.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*
import java.util.function.Function

class BukkitEntityModule : BukkitModule {

    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
            "attack" to {
                createBukkitTrigger(EntityDamageByEntityEvent::class.java, {
                    val attacker = it.damager
                    if (attacker is Player) attacker.uniqueId else if (attacker is Projectile) (attacker.shooter as? Player)?.uniqueId else null
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
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to {
                { event ->
                    event.unwrap ref@ { target: EntityEvent ->
                        val entity = target.entity as? LivingEntity ?: return@ref HudListener.EMPTY
                        entity.getAttribute(ATTRIBUTE_MAX_HEALTH)?.value?.let { maxHealth ->
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
            "last_damage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        (e.entity as? LivingEntity)?.lastDamage ?: 0.0
                    }
                }
            },
            "last_health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        (e.entity as? LivingEntity)?.let { le -> le.health + le.lastDamage } ?: 0.0
                    }
                }
            },
            "max_health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        (e.entity as? LivingEntity)?.maximumHealth ?: 0.0
                    }
                }
            },
            "health_percentage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function get@ {
                        val entity = e.entity as? LivingEntity ?: return@get 0.0
                        entity.health / entity.maximumHealth
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
                        e.entity.type.key.key
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
            }
        )

}