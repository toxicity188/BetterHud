package kr.toxicity.hud.compatibility.mmocore

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.compatibility.Compatibility
import kr.toxicity.hud.util.PLUGIN
import net.Indyuce.mmocore.api.MMOCoreAPI
import org.bukkit.configuration.ConfigurationSection

class MMOCoreCompatibility: Compatibility {
    private val api = MMOCoreAPI(PLUGIN)
    override val listeners: Map<String, (ConfigurationSection) -> HudListener>
        get() = mapOf(
            "mana" to { _ ->
                HudListener { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    mmo.mana / mmo.stats.getStat("MAX_MANA")
                }
            },
            "stamina" to { _ ->
                HudListener { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    mmo.stamina / mmo.stats.getStat("MAX_STAMINA")
                }
            },
            "stellium" to { _ ->
                HudListener { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    mmo.stellium / mmo.stats.getStat("MAX_STELLIUM")
                }
            },
            "experience" to { _ ->
                HudListener { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    mmo.experience / mmo.levelUpExperience
                }
            },
            "cooldown" to { c ->
                val slot = c.getInt("slot", 0)
                HudListener { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    (mmo.getBoundSkill(slot)?.let {
                        mmo.cooldownMap.getCooldown(it) / it.skill.getModifier("cooldown", mmo.getSkillLevel(it.skill))
                    } ?: 0.0)
                }
            }
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "mana" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).mana
            },
            "max_mana" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_MANA")
            },
            "stamina" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).stamina
            },
            "max_stamina" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_STAMINA")
            },
            "stellium" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).stellium
            },
            "max_stellium" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_STELLIUM")
            },
            "party_member_count" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).party?.countMembers() ?: 0
            },
            "guild_member_count" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).guild?.countMembers() ?: 0
            },
            "stat" to HudPlaceholder.of(1) { p, a ->
                api.getPlayerData(p.bukkitPlayer).stats.getStat(a[0])
            },
            "exp" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).experience
            },
            "claims" to HudPlaceholder.of(1) { p, a ->
                api.getPlayerData(p.bukkitPlayer).getClaims(a[0])
            },
            "cooldown" to HudPlaceholder.of(1) { p, a ->
                val mmo = api.getPlayerData(p.bukkitPlayer)
                mmo.getBoundSkill(a[0].toIntOrNull() ?: 0)?.skill?.let {
                    it.getModifier("cooldown", mmo.getSkillLevel(it))
                } ?: -1
            },
            "current_cooldown" to HudPlaceholder.of(1) { p, a ->
                val mmo = api.getPlayerData(p.bukkitPlayer)
                mmo.getBoundSkill(a[0].toIntOrNull() ?: 0)?.let {
                    mmo.cooldownMap.getCooldown(it)
                } ?: -1
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "guild_id" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).guild?.id ?: "<none>"
            },
            "guild_name" to HudPlaceholder.of(0) { p, _ ->
                api.getPlayerData(p.bukkitPlayer).guild?.name ?: "<none>"
            },
            "skill_name" to HudPlaceholder.of(1) { p, a ->
                val mmo = api.getPlayerData(p.bukkitPlayer)
                mmo.getBoundSkill(a[0].toIntOrNull() ?: 0)?.skill?.name ?: "<none>"
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(

        )
}