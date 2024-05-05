package kr.toxicity.hud.compatibility.mythicmobs.event

import io.lumine.mythic.api.adapters.AbstractPlayer
import io.lumine.mythic.api.skills.SkillCaster
import kr.toxicity.hud.api.event.BetterHudEvent
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class MythicMobsPopupEvent(
    val caster: SkillCaster,
    val target: AbstractPlayer
): Event(), BetterHudEvent {
    companion object {
        @Suppress("UNUSED")
        fun getHandlerList(): HandlerList = BetterHudEvent.HANDLER_LIST
    }
    override fun getHandlers(): HandlerList = BetterHudEvent.HANDLER_LIST
}