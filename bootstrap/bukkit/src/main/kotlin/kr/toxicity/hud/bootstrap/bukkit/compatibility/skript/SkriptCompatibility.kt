package kr.toxicity.hud.bootstrap.bukkit.compatibility.skript

import ch.njol.skript.Skript
import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.lang.VariableString
import ch.njol.skript.registrations.Classes
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.bukkit.event.HudUpdateEvent
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.update.UpdateReason
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.effect.*
import kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.expression.ExprHudPlayer
import kr.toxicity.hud.bootstrap.bukkit.util.unwrap
import kr.toxicity.hud.manager.HudManagerImpl
import kr.toxicity.hud.manager.PopupManagerImpl
import kr.toxicity.hud.util.ifNull
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.skriptlang.skript.registration.DefaultSyntaxInfos
import org.skriptlang.skript.registration.SyntaxInfo
import org.skriptlang.skript.registration.SyntaxRegistry
import java.util.function.Function

class SkriptCompatibility : Compatibility {

    override val website: String = "https://www.spigotmc.org/resources/114544/"

    override fun start() {
        Classes.registerClass(
            ClassInfo(
            Hud::class.java,
            "hud"
        ).parser(object : Parser<Hud>() {
            override fun toString(p0: Hud, p1: Int): String = p0.toString()
            override fun parse(s: String, context: ParseContext): Hud? = HudManagerImpl.getHud(s)
            override fun toVariableNameString(p0: Hud): String = p0.name
        }))
        Classes.registerClass(
            ClassInfo(
            Popup::class.java,
            "popup"
        ).parser(object : Parser<Popup>() {
            override fun toString(p0: Popup, p1: Int): String = p0.toString()
            override fun parse(s: String, context: ParseContext): Popup? = PopupManagerImpl.getPopup(s)
            override fun toVariableNameString(p0: Popup): String = p0.name
        }))

        Skript.instance().registerAddon(BetterHud::class.java, "betterhud").syntaxRegistry().run {
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffShowPopup::class.java).addPattern("[show] popup %string% to %players% [with [variable] [of] %-objects%] [keyed by %-object%]").build()
            )
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffCallPopupEvent::class.java).addPattern("call popup event for %players% named %string% [with [variable] [of] %-objects%] [keyed by %-object%]").build()
            )
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffClearPopup::class.java).addPattern("clear popup of %players%").build()
            )
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffUpdateHud::class.java).addPattern("update hud of %players%").build()
            )
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffPointAdd::class.java).addPattern("point add %location% named %string% [with icon %-string%] to %players%").build()
            )
            register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffPointRemove::class.java).addPattern("point remove %string% to %players%").build()
            )
            register(
                SyntaxRegistry.EXPRESSION,
                DefaultSyntaxInfos.Expression.builder(ExprHudPlayer::class.java, Player::class.java)
                    .priority(DefaultSyntaxInfos.Expression.SIMPLE)
                    .addPattern("hud player")
                    .build()
            )
        }
    }

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>> = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener> = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>> = mapOf()
    override val strings: Map<String, HudPlaceholder<String>> = mapOf(
        "variable" to HudPlaceholder.builder<String>()
            .requiredArgsLength(1)
            .function { args, reason ->
                val value by lazy {
                    VariableString.newInstance(args.joinToString(",")).ifNull { "Invalid variable." }
                }
                if (reason.type == UpdateReason.EMPTY) {
                    Function {
                        value.getSingle(HudUpdateEvent(it)) ?: "<none>"
                    }
                } else reason.unwrap<Event, Function<HudPlayer, String>> { e ->
                    Function {
                        value.getSingle(e) ?: "<none>"
                    }
                }
            }
            .build()
    )
    override val booleans: Map<String, HudPlaceholder<Boolean>> = mapOf()
}