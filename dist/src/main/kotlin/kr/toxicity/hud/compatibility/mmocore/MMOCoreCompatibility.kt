package kr.toxicity.hud.compatibility.mmocore

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.compatibility.Compatibility
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.ifNull
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.MMOCoreAPI
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

class MMOCoreCompatibility: Compatibility {
    private val api = MMOCoreAPI(PLUGIN)
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "mana" to { _ ->
                {
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.mana / mmo.stats.getStat("MAX_MANA")
                    }
                }
            },
            "stamina" to { _ ->
                {
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.stamina / mmo.stats.getStat("MAX_STAMINA")
                    }
                }
            },
            "stellium" to { _ ->
                {
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.stellium / mmo.stats.getStat("MAX_STELLIUM")
                    }
                }
            },
            "experience" to { _ ->
                {
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.experience / mmo.levelUpExperience
                    }
                }
            },
            "cooldown_slot" to search@ { c ->
                val slot = c.getInt("slot", 0)
                return@search {
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        (mmo.getBoundSkill(slot)?.let {
                            mmo.cooldownMap.getCooldown(it) / it.skill.getModifier("cooldown", mmo.getSkillLevel(it.skill))
                        } ?: 0.0)
                    }
                }
            },
            "cooldown_skill" to search@ { c ->
                val name = c.getString("skill").ifNull("skill value not set.")
                val skill = MMOCore.plugin.skillManager.getSkill(name).ifNull("the skill named \"$name\" doesn't exist.")
                return@search { _: UpdateEvent ->
                    HudListener { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.cooldownMap.getCooldown("skill_" + skill.handler.id) / skill.getModifier("cooldown", mmo.getSkillLevel(skill))
                    }
                }
            }
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "mana" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).mana
                }
            },
            "max_mana" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_MANA")
                }
            },
            "stamina" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).stamina
                }
            },
            "max_stamina" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_STAMINA")
                }
            },
            "stellium" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).stellium
                }
            },
            "max_stellium" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).stats.getStat("MAX_STELLIUM")
                }
            },
            "party_member_count" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).party?.countMembers() ?: 0
                }
            },
            "guild_member_count" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).guild?.countMembers() ?: 0
                }
            },
            "exp" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).experience
                }
            },
            "stat" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    return Function { p ->
                        api.getPlayerData(p.bukkitPlayer).stats.getStat(args[0])
                    }
                }
            },
            "stat" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    return Function { p ->
                        api.getPlayerData(p.bukkitPlayer).stats.getStat(args[0])
                    }
                }
            },
            "claims" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    return Function { p ->
                        api.getPlayerData(p.bukkitPlayer).getClaims(args[0])
                    }
                }
            },
            "claims" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val i = args[0].toInt()
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.getBoundSkill(i)?.skill?.let {
                            it.getModifier("cooldown", mmo.getSkillLevel(it))
                        } ?: -1
                    }
                }
            },
            "current_cooldown_slot" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val i = args[0].toInt()
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.getBoundSkill(i)?.skill?.let {
                            it.getModifier("cooldown", mmo.getSkillLevel(it))
                        } ?: -1
                    }
                }
            },
            "current_cooldown_skill" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.cooldownMap.getCooldown("skill_" + skill.handler.id)
                    }
                }
            },
            "skill_bound_index" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        (0..8).firstOrNull {
                            mmo.getBoundSkill(it)?.skill?.handler?.id == skill.handler.id
                        } ?: -1
                    }
                }
            },
            "skill_level" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        api.getPlayerData(p.bukkitPlayer).getSkillLevel(skill)
                    }
                }
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "guild_id" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).guild?.id ?: "<none>"
                }
            },
            "guild_name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    api.getPlayerData(p.bukkitPlayer).guild?.name ?: "<none>"
                }
            },
            "skill_name" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, String> {
                    val i = args[0].toInt()
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.getBoundSkill(i)?.skill?.name ?: "<none>"
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "is_casting_mode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    val mmo = api.getPlayerData(p.bukkitPlayer)
                    mmo.isCasting
                }
            },
            "bounded_skill" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Boolean> {
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.boundSkills.any {
                            it.skill.handler.id == args[0]
                        }
                    }
                }
            },
            "bounded_slot" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Boolean> {
                    val i = args[0].toInt()
                    return Function { p ->
                        val mmo = api.getPlayerData(p.bukkitPlayer)
                        mmo.getBoundSkill(i) != null
                    }
                }
            }
        )
}