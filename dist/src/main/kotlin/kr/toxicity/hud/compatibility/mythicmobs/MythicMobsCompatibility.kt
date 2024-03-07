package kr.toxicity.hud.compatibility.mythicmobs

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.AbstractSkill
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.compatibility.Compatibility
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

class MythicMobsCompatibility: Compatibility {
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = mapOf(

        )
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
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(

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
        )
}