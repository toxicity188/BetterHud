package kr.toxicity.hud.skript

import ch.njol.skript.Skript
import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.registrations.Classes
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.manager.BetterHudManager
import kr.toxicity.hud.manager.HudManagerImpl
import kr.toxicity.hud.manager.PopupManagerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.skript.effect.EffCallPopupEvent
import kr.toxicity.hud.skript.effect.EffShowPopup
import kr.toxicity.hud.skript.effect.EffUpdateHud
import kr.toxicity.hud.skript.expression.ExprHudPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object SkriptManager: BetterHudManager {
    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            Classes.registerClass(ClassInfo(
                Hud::class.java,
                "hud"
            ).parser(object : Parser<Hud>() {
                override fun toString(p0: Hud, p1: Int): String = p0.toString()
                override fun parse(s: String, context: ParseContext): Hud? = HudManagerImpl.getHud(s)
                override fun toVariableNameString(p0: Hud): String = p0.name
            }))
            Classes.registerClass(ClassInfo(
                Popup::class.java,
                "popup"
            ).parser(object : Parser<Popup>() {
                override fun toString(p0: Popup, p1: Int): String = p0.toString()
                override fun parse(s: String, context: ParseContext): Popup? = PopupManagerImpl.getPopup(s)
                override fun toVariableNameString(p0: Popup): String = p0.name
            }))


            Skript.registerEffect(EffShowPopup::class.java, "[show] popup %string% to %players% [with [variable] [of] %-objects%] [keyed by %-object%]")
            Skript.registerEffect(EffCallPopupEvent::class.java, "call popup event for %players% named %string% [with [variable] [of] %-objects%] [keyed by %-object%]")
            Skript.registerEffect(EffUpdateHud::class.java, "update hud of %players%")
            Skript.registerExpression(ExprHudPlayer::class.java, Player::class.java, ExpressionType.SIMPLE, "hud player")
        }
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        callback()
    }

    override fun end() {
    }
}