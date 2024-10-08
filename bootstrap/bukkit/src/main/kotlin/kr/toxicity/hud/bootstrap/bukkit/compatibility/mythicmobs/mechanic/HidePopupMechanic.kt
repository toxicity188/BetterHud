package kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs.mechanic

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractPlayer
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.INoTargetSkill
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillMechanic
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.manager.PopupManagerImpl
import org.bukkit.entity.Player

class HidePopupMechanic(mlc: MythicLineConfig): SkillMechanic(MythicBukkit.inst().skillManager, null, "[BetterHud]", mlc), INoTargetSkill, ITargetedEntitySkill {

    private val popup = mlc.getString(arrayOf("popup", "p"))

    override fun cast(p0: SkillMetadata): SkillResult {
        val caster = p0.caster
        if (caster is AbstractPlayer) {
            val bukkit = caster.bukkitEntity as? Player ?: return SkillResult.CONDITION_FAILED
            PopupManagerImpl.getPopup(popup)?.hide(PlayerManagerImpl.getHudPlayer(bukkit.uniqueId) ?: return SkillResult.CONDITION_FAILED)
        }
        return SkillResult.SUCCESS
    }

    override fun castAtEntity(p0: SkillMetadata, p1: AbstractEntity): SkillResult {
        if (p1 is AbstractPlayer) {
            val bukkit = p1.bukkitEntity as? Player ?: return SkillResult.CONDITION_FAILED
            PopupManagerImpl.getPopup(popup)?.hide(PlayerManagerImpl.getHudPlayer(bukkit.uniqueId) ?: return SkillResult.CONDITION_FAILED)
        }
        return SkillResult.SUCCESS
    }
}