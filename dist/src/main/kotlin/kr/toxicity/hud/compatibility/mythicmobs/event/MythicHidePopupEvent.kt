package kr.toxicity.hud.compatibility.mythicmobs.event

import io.lumine.mythic.api.adapters.AbstractPlayer
import io.lumine.mythic.api.skills.SkillCaster
import kr.toxicity.hud.api.event.BetterHudEvent
import org.bukkit.event.HandlerList

class MythicHidePopupEvent(caster: SkillCaster, target: AbstractPlayer): MythicMobsPopupEvent(caster, target) {
    companion object {
        @Suppress("UNUSED")
        fun getHandlerList(): HandlerList = BetterHudEvent.HANDLER_LIST
    }
}