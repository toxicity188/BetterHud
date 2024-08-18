package kr.toxicity.hud.bootstrap.bukkit.compatibility.mythiclib

import io.lumine.mythic.lib.api.event.PlayerAttackEvent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.util.createBukkitTrigger
import kr.toxicity.hud.bootstrap.bukkit.util.unwrap
import java.util.function.Function

class MythicLibCompatibility: Compatibility {
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf(
            "damage" to {
                val weaponCritical = it.getAsBoolean("weapon-critical", false)
                val skillCritical = it.getAsBoolean("skill-critical", false)
                createBukkitTrigger(PlayerAttackEvent::class.java, { e ->
                    val data = e.damage
                    if ((weaponCritical && !data.isWeaponCriticalStrike) || (skillCritical && !data.isSkillCriticalStrike)) null
                    else e.attacker.player.uniqueId
                }) { e ->
                    e.entity.uniqueId
                }
            }
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
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