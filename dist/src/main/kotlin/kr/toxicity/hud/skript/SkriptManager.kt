package kr.toxicity.hud.skript

import ch.njol.skript.Skript
import kr.toxicity.hud.manager.BetterHudManager
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.skript.effect.EffCallPopupEvent
import kr.toxicity.hud.skript.effect.EffShowPopup
import org.bukkit.Bukkit

object SkriptManager: BetterHudManager {
    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            Skript.registerEffect(EffShowPopup::class.java, "[show] popup %string% to %players% [with [variable] [of] %-objects%] [keyed by %-object%]")
            Skript.registerEffect(EffCallPopupEvent::class.java, "call popup event for %players% named %string% [with [variable] [of] %-objects%] [keyed by %-object%]")
        }
    }

    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}