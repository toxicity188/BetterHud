package kr.toxicity.hud.bootstrap.fabric.module.fabric

import kr.toxicity.hud.api.fabric.entity.FabricLivingEntity
import kr.toxicity.hud.api.fabric.trigger.HudFabricEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.fabric.module.FabricModule
import kr.toxicity.hud.bootstrap.fabric.util.armor
import kr.toxicity.hud.bootstrap.fabric.util.fabricPlayer
import kr.toxicity.hud.bootstrap.fabric.util.hasPermission
import kr.toxicity.hud.bootstrap.fabric.util.toMiniMessageString
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import java.util.function.Function

class FabricStandardModule : FabricModule {
    override val triggers: Map<String, (YamlObject) -> HudFabricEventTrigger<*>>
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
                        p.fabricPlayer.experienceProgress.toDouble()
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
            "last_damage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer as FabricLivingEntity).`betterhud$getLastDamage`()
                }
            },
            "last_health" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer as FabricLivingEntity).`betterhud$getLastHealth`()
                }
            },
            "last_health_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.fabricPlayer as FabricLivingEntity).let {
                        it.`betterhud$getLastHealth`() / (it as LivingEntity).maxHealth
                    }
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
            "potion_effect_duration" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val location = ResourceLocation.withDefaultNamespace(args[0])
                    Function { p ->
                        p.fabricPlayer.activeEffects.firstOrNull {
                            it.effect.`is`(location)
                        }?.duration ?: 0
                    }
                }
                .build(),
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
                    p.fabricPlayer.name.toMiniMessageString()
                }
            },
            "gamemode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.gameMode.gameModeForPlayer.name
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
                    p.fabricPlayer.isFullyFrozen
                }
            },
            "burning" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.fabricPlayer.remainingFireTicks > 0
                }
            },
            "has_permission" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function {
                        it.fabricPlayer.hasPermission(args[0])
                    }
                }
                .build(),
            "has_main_hand" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    !p.fabricPlayer.mainHandItem.`is`(ItemStack.EMPTY.item)
                }
            },
            "has_off_hand" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    !p.fabricPlayer.offhandItem.`is`(ItemStack.EMPTY.item)
                }
            },
        )
}