package kr.toxicity.hud.compatibility.mythicmobs

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.AbstractSkill
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.compatibility.Compatibility
import org.bukkit.configuration.ConfigurationSection

class MythicMobsCompatibility: Compatibility {
    override val listeners: Map<String, (ConfigurationSection) -> HudListener>
        get() = mapOf(

        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "current_cooldown" to HudPlaceholder.of(1) { player, a ->
                val entity = BukkitAdapter.adapt(player.bukkitPlayer)
                MythicBukkit.inst().skillManager.getSkill(a[0]).map {
                    (it as AbstractSkill).getCooldown(object : SkillCaster {
                        override fun getEntity(): AbstractEntity = entity
                        override fun setUsingDamageSkill(p0: Boolean) {}
                        override fun isUsingDamageSkill(): Boolean = false
                    })
                }.orElse(0F).toDouble()
            },
            "aura_stack" to HudPlaceholder.of(1) { player, a ->
                MythicBukkit.inst().playerManager.getProfile(player.bukkitPlayer).getAuraStacks(a[0])
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(

        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "hasaura" to HudPlaceholder.of(1) { p, a ->
                MythicBukkit.inst().playerManager.getProfile(p.bukkitPlayer).auraRegistry.hasAura(a[0])
            }
        )
}