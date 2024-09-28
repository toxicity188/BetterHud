package kr.toxicity.hud.bootstrap.bukkit.compatibility.mmocore

import io.lumine.mythic.lib.player.modifier.ModifierSource
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.util.ifNull
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import kr.toxicity.hud.api.yaml.YamlObject
import org.bukkit.entity.Player
import java.util.function.Function

class MMOCoreCompatibility: Compatibility {

    private fun Player.toMMOCore(): PlayerData? {
        return MMOCore.plugin.playerDataManager.getOrNull(uniqueId)
    }

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()

    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "mana" to { _ ->
                {
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        mmo.mana / mmo.stats.getStat("MAX_MANA")
                    }
                }
            },
            "stamina" to { _ ->
                {
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        mmo.stamina / mmo.stats.getStat("MAX_STAMINA")
                    }
                }
            },
            "stellium" to { _ ->
                {
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        mmo.stellium / mmo.stats.getStat("MAX_STELLIUM") * 100
                    }
                }
            },
            "experience" to { _ ->
                {
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        mmo.experience / mmo.levelUpExperience
                    }
                }
            },
            "cooldown_slot" to search@ { c ->
                val slot = c.getAsInt("slot", 0)
                return@search {
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        (mmo.getBoundSkill(slot)?.let {
                            (mmo.cooldownMap.getCooldown(it) / it.skill.getModifier("cooldown", mmo.getSkillLevel(it.skill))).coerceAtLeast(0.0)
                        } ?: 0.0)
                    }
                }
            },
            "cooldown_skill" to search@ { c ->
                val name = c.get("skill")?.asString().ifNull("skill value not set.")
                val skill = MMOCore.plugin.skillManager.getSkill(name).ifNull("the skill named \"$name\" doesn't exist.")
                return@search { _: UpdateEvent ->
                    HudListener { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                        (mmo.cooldownMap.getCooldown("skill_" + skill.handler.id) / skill.getModifier("cooldown", mmo.getSkillLevel(skill))).coerceAtLeast(0.0)
                    }
                }
            }
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "mana" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).mana
                }
            },
            "max_mana" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.getStat("MAX_MANA")
                }
            },
            "mana_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).let {
                        it.mana / it.stats.getStat("MAX_MANA") * 100
                    }
                }
            },
            "stamina" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stamina
                }
            },
            "max_stamina" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.getStat("MAX_STAMINA")
                }
            },
            "stamina_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).let {
                        it.stamina / it.stats.getStat("MAX_STAMINA") * 100
                    }
                }
            },
            "stellium" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stellium
                }
            },
            "max_stellium" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.getStat("MAX_STELLIUM")
                }
            },
            "stellium_percentage" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).let {
                        it.stellium / it.stats.getStat("MAX_STELLIUM") * 100
                    }
                }
            },
            "party_member_count" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).party?.countMembers() ?: 0
                }
            },
            "guild_member_count" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).guild?.countMembers() ?: 0
                }
            },
            "exp" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).experience
                }
            },
            "max_exp" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).levelUpExperience
                }
            },
            "level" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).level
                }
            },
            "stat" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    return Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.getStat(args[0])
                    }
                }
            },
            "temp_stat" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    return Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.map.getInstance(args[0]).getFilteredTotal {
                            it.source == ModifierSource.OTHER
                        }
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
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).getClaims(args[0])
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
                        mmo.cooldownMap.getCooldown("skill_" + skill.handler.id)
                    }
                }
            },
            "required_mana_skill" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
                        skill.getModifier("mana", mmo.getSkillLevel(skill))
                    }
                }
            },
            "required_stamina_skill" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
                        skill.getModifier("stamina", mmo.getSkillLevel(skill))
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0)
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
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).getSkillLevel(skill)
                    }
                }
            },
            "casting_slot" to object : HudPlaceholder<Number> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(
                    args: MutableList<String>,
                    reason: UpdateEvent
                ): Function<HudPlayer, Number> {
                    val skill = MMOCore.plugin.skillManager.getSkill(args[0]) ?: throw RuntimeException("Unable to find that skill: ${args[0]}")
                    return Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).boundSkills.entries.firstOrNull {
                            it.value.classSkill.skill.handler.id == skill.handler.id
                        }?.key ?: 0
                    }
                }
            },
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "class" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function "<none>").profess.name
                }
            },
            "guild_id" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function "<none>").guild?.id ?: "<none>"
                }
            },
            "guild_name" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    (p.bukkitPlayer.toMMOCore() ?: return@Function "<none>").guild?.name ?: "<none>"
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function "<none>")
                        mmo.getBoundSkill(i)?.skill?.name ?: "<none>"
                    }
                }
            },
            "party_member" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val index = args[0].toInt()
                    return Function get@ { p ->
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@get "<none>")
                        mmo.party?.onlineMembers?.let {
                            return@get if (index < it.size) it[index].player.name else "<none>"
                        }
                        return@get "<none>"
                    }
                }
            },
            "party_member_exclude_mine" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val index = args[0].toInt()
                    return Function get@ { p ->
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@get "<none>")
                        val uuid = p.bukkitPlayer.uniqueId
                        mmo.party?.onlineMembers?.filter {
                            it.player.uniqueId != uuid
                        }?.let {
                            return@get if (index < it.size) it[index].player.name else "<none>"
                        }
                        return@get "<none>"
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "is_casting_mode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function false)
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function false)
                        mmo.boundSkills.any {
                            it.value.classSkill.skill.handler.id == args[0]
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
                        val mmo = (p.bukkitPlayer.toMMOCore() ?: return@Function false)
                        mmo.getBoundSkill(i) != null
                    }
                }
            }
        )
}