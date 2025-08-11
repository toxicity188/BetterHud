package kr.toxicity.hud.bootstrap.bukkit.compatibility

import io.lumine.mythic.lib.api.player.MMOPlayerData
import io.lumine.mythic.lib.player.cooldown.CooldownObject
import io.lumine.mythic.lib.skill.handler.SkillHandler

typealias MMOCorePlayer = net.Indyuce.mmocore.api.player.PlayerData
typealias MMOItemsPlayer = net.Indyuce.mmoitems.api.player.PlayerData

typealias MMOCoreSkill = net.Indyuce.mmocore.skill.RegisteredSkill
typealias MMOItemsSkill = net.Indyuce.mmoitems.skill.RegisteredSkill

fun MMOCorePlayer.cooldown(skill: CooldownObject) = mmoPlayerData.cooldown(skill)
fun MMOCorePlayer.cooldown(handler: SkillHandler<*>) = mmoPlayerData.cooldown(handler)
fun MMOCorePlayer.cooldown(skill: MMOCoreSkill) = mmoPlayerData.cooldown(skill.handler)

fun MMOItemsPlayer.cooldown(skill: CooldownObject) = mmoPlayerData.cooldown(skill)
fun MMOItemsPlayer.cooldown(handler: SkillHandler<*>) = mmoPlayerData.cooldown(handler)
fun MMOItemsPlayer.cooldown(skill: MMOItemsSkill) = mmoPlayerData.cooldown(skill.handler)

fun MMOPlayerData.cooldown(skill: CooldownObject) = cooldownMap.getCooldown(skill)
fun MMOPlayerData.cooldown(handler: SkillHandler<*>) = cooldownMap.getCooldown("skill_" + handler.id)