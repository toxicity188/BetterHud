package kr.toxicity.hud.bootstrap.bukkit.compatibility.mmocore

import io.lumine.mythic.lib.MythicLib
import io.lumine.mythic.lib.api.stat.modifier.StatModifier
import io.lumine.mythic.lib.player.modifier.ModifierSource
import io.lumine.mythic.lib.skill.handler.SkillHandler
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.cooldown
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.util.ifNull
import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.stats.PlayerStats
import org.bukkit.entity.Player
import java.util.function.Function

class MMOCoreCompatibility : Compatibility {

    override val website: String = "https://www.spigotmc.org/resources/70575/"

    private fun Player.toMMOCore(): PlayerData? {
        return MMOCore.plugin.playerDataManager.getOrNull(uniqueId)
    }

    private fun PlayerData.modifier(skill: SkillHandler<*>, key: String) = getSkillLevel(skill).let { level ->
        profess.getSkill(skill.id)?.getParameter(key, level, this) ?: skill.getDefaultFormula(key).evaluate(level, player)
    }
    
    private fun skill(name: String) = MythicLib.plugin.skills.getHandler(name) ?: throw RuntimeException("Unable to find that skill: $name")

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()

    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
            "mana" to { _ ->
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    mmo.mana / mmo.stats.getStat("MAX_MANA")
                }.run {
                    { this }
                }
            },
            "stamina" to { _ ->
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    mmo.stamina / mmo.stats.getStat("MAX_STAMINA")
                }.run {
                    { this }
                }
            },
            "stellium" to { _ ->
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    mmo.stellium / mmo.stats.getStat("MAX_STELLIUM") * 100
                }.run {
                    { this }
                }
            },
            "experience" to { _ ->
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    mmo.experience / mmo.levelUpExperience
                }.run {
                    { this }
                }
            },
            "cooldown_slot" to { c ->
                val slot = c.getAsInt("slot", 0)
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    mmo.getBoundSkill(slot)?.let {
                        (mmo.cooldown(it) / mmo.modifier(it.skill, "cooldown")).coerceAtLeast(0.0)
                    } ?: 0.0
                }.run {
                    { this }
                }
            },
            "cooldown_skill" to { c ->
                val skill = skill(c["skill"]?.asString().ifNull { "skill value not set." })
                HudListener { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@HudListener 0.0
                    (mmo.cooldown(skill) / mmo.modifier(skill, "cooldown")).coerceAtLeast(0.0)
                }.run {
                    { this }
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
            "stat" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val getter: (PlayerStats) -> Number = if (args.size > 1) {
                        { stats: PlayerStats ->
                            stats.map.getInstance(args[0]).getFilteredTotal {
                                it.key == args[1]
                            }
                        }
                    } else {
                        { stats: PlayerStats ->
                            stats.getStat(args[0])
                        }
                    }
                    Function { p ->
                        getter((p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats)
                    }
                }
                .build(),
            "temp_stat" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val predicate: (StatModifier) -> Boolean = if (args.size > 1) {
                        { stat: StatModifier ->
                            stat.source == ModifierSource.OTHER && stat.key == args[1]
                        }
                    } else {
                        { stat: StatModifier ->
                            stat.source == ModifierSource.OTHER
                        }
                    }
                    Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).stats.map.getInstance(args[0]).getFilteredTotal(predicate)
                    }
                }
                .build(),
            "claims" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).getClaims(args[0])
                    }
                }
                .build(),
            "claims" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val i = args[0].toInt()
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function 0.0
                        mmo.getBoundSkill(i)?.getParameter("cooldown", mmo) ?: -1
                    }
                }
                .build(),
            "current_cooldown_slot" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val i = args[0].toInt()
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function 0.0
                        mmo.getBoundSkill(i)?.getParameter("cooldown", mmo) ?: -1
                    }
                }
                .build(),
            "current_cooldown_skill" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        p.bukkitPlayer.toMMOCore()?.cooldown(skill) ?: return@Function 0.0
                    }
                }
                .build(),
            "required_mana_skill" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function 0.0
                        mmo.modifier(skill, "mana")
                    }
                }
                .build(),
            "required_stamina_skill" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function 0.0
                        mmo.modifier(skill, "stamina")
                    }
                }
                .build(),
            "skill_bound_index" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function 0.0
                        (0..8).firstOrNull {
                            mmo.getBoundSkill(it)?.skill?.id == skill.id
                        } ?: -1
                    }
                }
                .build(),
            "skill_level" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).getSkillLevel(skill)
                    }
                }
                .build(),
            "casting_slot" to HudPlaceholder.builder<Number>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val skill = skill(args[0])
                    Function { p ->
                        val bar = p.bukkitPlayer.inventory.heldItemSlot
                        var i = 0
                        for ((index, entry) in (p.bukkitPlayer.toMMOCore() ?: return@Function 0.0).boundSkills.entries.withIndex()) {
                            if (entry.value.classSkill.skill.id == skill.id) {
                                i = entry.key
                                if (index >= bar) i++
                                break
                            }
                        }
                        i
                    }
                }
                .build(),
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
            "skill_name" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val i = args[0].toInt()
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function "<none>"
                        mmo.getBoundSkill(i)?.skill?.name ?: "<none>"
                    }
                }
                .build(),
            "party_member" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val index = args[0].toInt()
                    Function get@ { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@get "<none>"
                        mmo.party?.onlineMembers?.let {
                            return@get if (index < it.size) it[index].player.name else "<none>"
                        }
                        return@get "<none>"
                    }
                }
                .build(),
            "party_member_exclude_mine" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val index = args[0].toInt()
                    Function get@ { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@get "<none>"
                        val uuid = p.bukkitPlayer.uniqueId
                        mmo.party?.onlineMembers?.filter {
                            it.player.uniqueId != uuid
                        }?.let {
                            return@get if (index < it.size) it[index].player.name else "<none>"
                        }
                        return@get "<none>"
                    }
                }
                .build()
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "is_loaded" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    p.bukkitPlayer.toMMOCore() != null
                }
            },
            "is_casting_mode" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function false
                    mmo.isCasting
                }
            },
            "bounded_skill" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function false
                        mmo.boundSkills.any {
                            it.value.classSkill.skill.id == args[0]
                        }
                    }
                }
                .build(),
            "bounded_slot" to HudPlaceholder.builder<Boolean>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val i = args[0].toInt()
                    Function { p ->
                        val mmo = p.bukkitPlayer.toMMOCore() ?: return@Function false
                        mmo.getBoundSkill(i) != null
                    }
                }
                .build()
        )
}