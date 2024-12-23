package kr.toxicity.hud.bootstrap.fabric.module.fabric

import kr.toxicity.hud.api.fabric.entity.FabricLivingEntity
import kr.toxicity.hud.api.fabric.event.EntityEvent
import kr.toxicity.hud.api.fabric.event.entity.PlayerAttackEntityEvent
import kr.toxicity.hud.api.fabric.event.entity.PlayerDamageByEntityEvent
import kr.toxicity.hud.api.fabric.event.entity.PlayerDeathEvent
import kr.toxicity.hud.api.fabric.event.entity.PlayerKillEntityEvent
import kr.toxicity.hud.api.fabric.trigger.HudFabricEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.fabric.module.FabricModule
import kr.toxicity.hud.bootstrap.fabric.util.createFabricTrigger
import kr.toxicity.hud.bootstrap.fabric.util.toMiniMessageString
import kr.toxicity.hud.bootstrap.fabric.util.unwrap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.*
import java.util.function.Function

class FabricEntityModule : FabricModule {

    override val triggers: Map<String, (YamlObject) -> HudFabricEventTrigger<*>>
        get() = mapOf(
            "attack" to {
                createFabricTrigger(PlayerAttackEntityEvent.REGISTRY, {
                    it.player.uuid
                }, {
                    it.entity.uuid
                })
            },
            "damage" to {
                createFabricTrigger(PlayerDamageByEntityEvent.REGISTRY, {
                    val victim = it.entity
                    if (victim is ServerPlayer) victim.uuid else null
                }, {
                    it.entity.uuid
                })
            },
            "dead" to {
                createFabricTrigger(PlayerDeathEvent.REGISTRY, {
                    it.player.uuid
                }, {
                    UUID.randomUUID()
                })
            },
            "kill" to {
                createFabricTrigger(PlayerKillEntityEvent.REGISTRY, {
                    it.player.uuid
                }, {
                    it.entity.uuid
                })
            },
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "health" to {
                { event ->
                    event.unwrap ref@ { target: EntityEvent<*> ->
                        val entity = target.entity() as? LivingEntity ?: return@ref HudListener.EMPTY
                        entity.getAttribute(Attributes.MAX_HEALTH)?.value?.let { maxHealth ->
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
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        (e.entity() as? LivingEntity)?.health ?: 0.0
                    }
                }
            },
            "last_damage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        (e.entity() as? FabricLivingEntity)?.`betterhud$getLastDamage`()
                    }
                }
            },
            "last_health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        (e.entity() as? FabricLivingEntity)?.`betterhud$getLastHealth`()
                    }
                }
            },
            "last_health_percentage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        (e.entity() as? FabricLivingEntity)?.let {
                            it.`betterhud$getLastHealth`() / (it as LivingEntity).maxHealth
                        }
                    }
                }
            },
            "max_health" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        (e.entity() as? LivingEntity)?.maxHealth ?: 0.0
                    }
                }
            },
            "health_percentage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function get@ {
                        val entity = e.entity() as? LivingEntity ?: return@get 0.0
                        entity.health / entity.maxHealth
                    }
                }
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        e.entity().name.toMiniMessageString()
                    }
                }
            },
            "type" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        BuiltInRegistries.ENTITY_TYPE.getKey(e.entity().type).path
                    }
                }
            },
            "custom_name" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        e.entity().customName?.toMiniMessageString() ?: e.entity().name.toMiniMessageString()
                    }
                }
            },
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "dead" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent<*> ->
                    Function {
                        e.entity().isDeadOrDying
                    }
                }
            }
        )

}