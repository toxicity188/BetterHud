package kr.toxicity.hud.bootstrap.bukkit.module.bukkit

import kr.toxicity.hud.api.bukkit.event.CustomPopupEvent
import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.module.BukkitModule
import kr.toxicity.hud.bootstrap.bukkit.util.*
import kr.toxicity.hud.util.ifNull
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.potion.PotionEffectType
import java.util.function.Function

class BukkitStandardModule : BukkitModule {
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
            "custom" to {
                val n = it.get("name")?.asString().ifNull { "name value not set." }
                createBukkitTrigger(CustomPopupEvent::class.java, { e ->
                    if (e.name == n) e.player.uniqueId else null
                })
            },
            "death" to {
                createBukkitTrigger(PlayerDeathEvent::class.java, {
                    it.entity.uniqueId
                })
            }
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.health / p.bukkitPlayer.maximumHealth
                    }
                }
            },
            "vehicle_health" to { _ ->
                {
                    HudListener { p ->
                        (p.bukkitPlayer.vehicle as? LivingEntity)?.let { entity ->
                            entity.health / entity.maximumHealth
                        } ?: 0.0
                    }
                }
            },
            "food" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.foodLevel / 20.0
                    }
                }
            },
            "armor" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.armor / 20.0
                    }
                }
            },
            "air" to { _ ->
                {
                    HudListener { p ->
                        (p.bukkitPlayer.remainingAir.toDouble() / p.bukkitPlayer.maximumAir).coerceAtLeast(0.0)
                    }
                }
            },
            "exp" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.exp.toDouble()
                    }
                }
            },
            "absorption" to { _ ->
                {
                    HudListener { p ->
                        p.bukkitPlayer.absorptionAmount / p.bukkitPlayer.maximumHealth
                    }
                }
            },
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "empty_space" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.emptySpace
                }
            },
            "health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.health
                }
            },
            "last_damage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.lastDamage
                }
            },
            "last_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.let { bp -> bp.health + bp.lastDamage }
                }
            },
            "last_health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.let { bp ->
                        (bp.health + bp.lastDamage) / bp.maximumHealth
                    }
                }
            },
            "vehicle_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.health ?: 0.0
                }
            },
            "food" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.foodLevel
                }
            },
            "armor" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.armor
                }
            },
            "air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.remainingAir
                }
            },
            "max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumHealth
                }
            },
            "health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    val bukkit = p.bukkitPlayer
                    bukkit.health / bukkit.maximumHealth
                }
            },
            "vehicle_max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.maximumHealth ?: 0.0
                }
            },
            "max_health_with_absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumHealth + p.bukkitPlayer.absorptionAmount
                }
            },
            "vehicle_max_health_with_absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.let { entity ->
                        entity.maximumHealth + entity.absorptionAmount
                    } ?: 0.0
                }
            },
            "vehicle_health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.let { entity ->
                        entity.health / entity.maximumHealth * 100.0
                    } ?: 0.0
                }
            },
            "max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.maximumAir
                }
            },
            "level" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.level
                }
            },
            "hotbar_slot" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.inventory.heldItemSlot
                }
            },
            "potion_effect_duration" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val potion = runCatching {
                        NamespacedKey.fromString(args[0])?.let { key ->
                            Registry.EFFECT.get(key)
                        }
                    }.onFailure {
                        @Suppress("DEPRECATION")
                        PotionEffectType.getByName(args[0])
                    }.getOrNull() ?: throw RuntimeException("this potion effect doesn't exist: ${args[0]}")
                    Function { p ->
                        p.bukkitPlayer.getPotionEffect(potion)?.duration ?: 0
                    }
                }
                .build(),
            "total_amount" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val item = Material.valueOf(args[0].uppercase())
                    Function { p ->
                        p.bukkitPlayer.totalAmount(item)
                    }
                }
                .build(),
            "storage" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val item = Material.valueOf(args[0].uppercase())
                    Function { p ->
                        p.bukkitPlayer.storage(item)
                    }
                }
                .build(),
            "absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.absorptionAmount
                }
            },
            "vehicle_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.remainingAir ?: 0
                }
            },
            "vehicle_max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.vehicle as? LivingEntity)?.maximumAir ?: 0
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.name
                }
            },
            "world" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.world.name
                }
            },
            "gamemode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.gameMode.name
                }
            },
            "custom_variable" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, reason ->
                    val key = args[0]
                    reason.unwrap { e: CustomPopupEvent ->
                        Function { _ ->
                            e.variables[key] ?: "<none>"
                        }
                    }
                }
                .build()
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "dead" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.isDead
                }
            },
            "frozen" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.isFrozen
                }
            },
            "burning" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.fireTicks > 0
                }
            },
            "has_off_hand" to HudPlaceholder.of { _, _ ->
                Function {
                    it.bukkitPlayer.inventory.itemInOffHand.type != Material.AIR
                }
            },
            "has_main_hand" to HudPlaceholder.of { _, _ ->
                Function {
                    it.bukkitPlayer.inventory.itemInMainHand.type != Material.AIR
                }
            },
            "has_permission" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function {
                        it.bukkitPlayer.hasPermission(args[0])
                    }
                }
                .build()
        )
}