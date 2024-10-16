package kr.toxicity.hud.bootstrap.fabric.module.fabric

import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.fabric.util.armor
import kr.toxicity.hud.bootstrap.fabric.util.fabricPlayer
import kr.toxicity.hud.bootstrap.fabric.util.hasPermission
import kr.toxicity.hud.bootstrap.fabric.module.Module
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.function.Function

class FabricStandardModule : Module {
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to { _ ->
                {
                    HudListener { p ->
                        p.fabricPlayer.health / p.fabricPlayer.getAttribute(Attributes.MAX_HEALTH)!!.value
                    }
                }
            },
            "vehicle_health" to { _ ->
                {
                    HudListener { p ->
                        (p.fabricPlayer.vehicle as? LivingEntity)?.let { entity ->
                            entity.health / entity.getAttribute(Attributes.MAX_HEALTH)!!.value
                        } ?: 0.0
                    }
                }
            },
            "food" to { _ ->
                {
                    HudListener { p ->
                        p.fabricPlayer.foodData.foodLevel / 20.0
                    }
                }
            },
            "armor" to { _ ->
                {
                    HudListener { p ->
                        p.fabricPlayer.armor / 20.0
                    }
                }
            },
            "air" to { _ ->
                {
                    HudListener { p ->
                        (p.fabricPlayer.airSupply.toDouble() / p.fabricPlayer.maxAirSupply).coerceAtLeast(0.0)
                    }
                }
            },
            "exp" to { _ ->
                {
                    HudListener { p ->
                        p.fabricPlayer.experienceLevel.toDouble()
                    }
                }
            },
            "absorption" to { _ ->
                {
                    HudListener { p ->
                        p.fabricPlayer.absorptionAmount / p.fabricPlayer.getAttribute(Attributes.MAX_HEALTH)!!.value
                    }
                }
            },
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.health
                }
            },
            "vehicle_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.health ?: 0.0
                }
            },
            "food" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.foodData.foodLevel
                }
            },
            "armor" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.armor
                }
            },
            "max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.getAttribute(Attributes.MAX_HEALTH)!!.value
                }
            },
            "vehicle_max_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.getAttribute(Attributes.MAX_HEALTH)?.value ?: 0.0
                }
            },
            "max_health_with_absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.getAttribute(Attributes.MAX_HEALTH)!!.value + p.fabricPlayer.absorptionAmount
                }
            },
            "vehicle_max_health_with_absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.let { entity ->
                        entity.getAttribute(Attributes.MAX_HEALTH)!!.value + entity.absorptionAmount
                    } ?: 0.0
                }
            },
            "health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.health / p.fabricPlayer.getAttribute(Attributes.MAX_HEALTH)!!.value * 100.0
                }
            },
            "vehicle_health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.let { entity ->
                        entity.health / entity.getAttribute(Attributes.MAX_HEALTH)!!.value * 100.0
                    } ?: 0.0
                }
            },
            "level" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.experienceLevel
                }
            },
            "hotbar_slot" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.inventory.selected
                }
            },
            "number" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        p.variableMap[args[0]]?.toDoubleOrNull() ?: 0.0
                    }
                }
            },
            "potion_effect_duration" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val location = ResourceLocation.withDefaultNamespace(args[0])
                    return Function { p ->
                        p.fabricPlayer.activeEffects.firstOrNull { 
                            it.effect.`is`(location)
                        }?.duration ?: 0
                    }
                }
            },
            "air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.airSupply
                }
            },
            "absorption" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.absorptionAmount
                }
            },
            "max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.maxAirSupply
                }
            },
            "vehicle_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.airSupply ?: 0
                }
            },
            "vehicle_max_air" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer.vehicle as? LivingEntity)?.maxAirSupply ?: 0
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.scoreboardName
                }
            },
            "gamemode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.gameMode.gameModeForPlayer.name
                }
            },
            "string" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    return Function { p ->
                        p.variableMap[args[0]] ?: "<none>"
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "dead" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.isDeadOrDying
                }
            },
            "frozen" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.isFreezing
                }
            },
            "has_permission" to object : HudPlaceholder<Boolean> {
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Boolean> {
                    return Function {
                        it.fabricPlayer.hasPermission(args[0])
                    }
                }

                override fun getRequiredArgsLength(): Int = 1
            }
        )
}