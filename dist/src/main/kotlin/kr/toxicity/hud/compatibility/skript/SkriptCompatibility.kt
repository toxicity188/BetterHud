package kr.toxicity.hud.compatibility.skript

import ch.njol.skript.lang.VariableString
import kr.toxicity.hud.api.event.HudUpdateEvent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.update.UpdateReason
import kr.toxicity.hud.compatibility.Compatibility
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.unwrap
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Event
import java.util.function.Function

class SkriptCompatibility: Compatibility {
    override val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "variable" to object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val value = VariableString.newInstance(args.joinToString(",")).ifNull("Invalid variable.")
                    return if (reason.type == UpdateReason.EMPTY) {
                        Function {
                            value.getSingle(HudUpdateEvent(it)) ?: "<none>"
                        }
                    } else reason.unwrap<Event, Function<HudPlayer, String>> { e ->
                        Function {
                            value.getSingle(e) ?: "<none>"
                        }
                    }
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()
}