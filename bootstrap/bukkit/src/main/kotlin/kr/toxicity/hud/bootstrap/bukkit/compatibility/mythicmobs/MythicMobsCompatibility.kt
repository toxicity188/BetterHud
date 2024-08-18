package kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.players.PlayerData
import io.lumine.mythic.core.skills.AbstractSkill
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs.event.MythicMobsPopupEvent
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs.mechanic.HidePopupMechanic
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs.mechanic.ShowPopupMechanic
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import org.bukkit.Bukkit
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.util.unwrap
import kr.toxicity.hud.util.BOOTSTRAP
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityEvent
import org.bukkit.plugin.Plugin
import java.util.function.Function

class MythicMobsCompatibility: Compatibility {
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "current_cooldown" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    val skill = MythicBukkit.inst().skillManager.getSkill(args[0]).orElseThrow {
                        RuntimeException("this skill doesn't exist: ${args[0]}")
                    } as AbstractSkill
                    return Function { p ->
                        skill.getCooldown(object : SkillCaster {
                            override fun getEntity(): AbstractEntity = BukkitAdapter.adapt(p.bukkitPlayer)
                            override fun setUsingDamageSkill(p0: Boolean) {}
                            override fun isUsingDamageSkill(): Boolean = false
                        })
                    }
                }
            },
            "aura_stack" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        MythicBukkit.inst().playerManager.getProfile(p.bukkitPlayer).getAuraStacks(args[0])
                    }
                }
            },
            "aura_max_duration" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        MythicBukkit.inst().playerManager.getProfile(p.bukkitPlayer).auraRegistry.auras[args[0]]?.maxOfOrNull {
                            it.startDuration
                        } ?: 0
                    }
                }
            },
            "aura_duration" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Number> {
                    return Function { p ->
                        MythicBukkit.inst().playerManager.getProfile(p.bukkitPlayer).auraRegistry.auras[args[0]]?.maxOfOrNull {
                            it.ticksRemaining
                        } ?: 0
                    }
                }
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "caster_variable" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val value = args[0]
                    return reason.unwrap { e: MythicMobsPopupEvent ->
                        val registry = when (val caster = e.caster) {
                            is PlayerData -> caster.variables.get(value)
                            is ActiveMob -> MythicBukkit.inst().variableManager.getRegistry(caster).get(value)
                            else -> null
                        }
                        Function {
                            registry?.get()?.toString() ?: "<none>"
                        }
                    }
                }
            },
            "target_variable" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val value = args[0]
                    return reason.unwrap { e: MythicMobsPopupEvent ->
                        val registry = MythicBukkit.inst().playerManager.getProfile(e.target).variables.get(value)
                        Function {
                            registry?.get()?.toString() ?: "<none>"
                        }
                    }
                }
            },
            "world_variable" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val registry = MythicBukkit.inst().variableManager.globalRegistry.get(args[0])
                    return Function {
                        registry?.get()?.toString() ?: "<none>"
                    }
                }
            },
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "has_aura" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Boolean> {
                    return Function { p ->
                        MythicBukkit.inst().playerManager.getProfile(p.bukkitPlayer).auraRegistry.hasAura(args[0])
                    }
                }
            },
            "is_mythicmob" to HudPlaceholder.of { _, u ->
                u.unwrap { e: EntityEvent ->
                    Function {
                        MythicBukkit.inst().mobManager.isMythicMob(e.entity)
                    }
                }
            }
        )

    init {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun load(e: MythicMechanicLoadEvent) {
                when (e.mechanicName.lowercase()) {
                    "showpopup" -> e.register(ShowPopupMechanic(e.config))
                    "hidepopup" -> e.register(HidePopupMechanic(e.config))
                }
            }
        }, BOOTSTRAP as Plugin)
    }
}