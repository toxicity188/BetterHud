package kr.toxicity.hud.compatibility.mythiclib

import io.lumine.mythic.lib.api.event.PlayerAttackEvent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.compatibility.Compatibility
import kr.toxicity.hud.util.createBukkitTrigger
import kr.toxicity.hud.util.unwrap
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

class MythicLibCompatibility: Compatibility {
    override val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>
        get() = mapOf(
            "damage" to {
                val weaponCritical = it.getBoolean("weapon-critical")
                val skillCritical = it.getBoolean("skill-critical")
                createBukkitTrigger(PlayerAttackEvent::class.java, { e ->
                    val data = e.damage
                    if ((weaponCritical && !data.isWeaponCriticalStrike) || (skillCritical && !data.isSkillCriticalStrike)) null
                    else e.attacker.player.uniqueId
                }) { e ->
                    e.entity.uniqueId
                }
            }
        )
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "attack_damage" to HudPlaceholder.of { _, u ->
                u.unwrap { e: PlayerAttackEvent ->
                    Function {
                        e.damage.damage
                    }
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()
}