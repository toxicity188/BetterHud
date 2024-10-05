package kr.toxicity.hud.bootstrap.fabric.compatibility

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.fabric.util.fabricPlayer
import net.kyori.adventure.platform.fabric.impl.WrappedComponent
import net.kyori.adventure.text.Component
import java.util.function.Function

class TextPlaceholderAPICompatibility: Compatibility {
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "parse" to object : HudPlaceholder<String> {
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val comp = WrappedComponent(
                        Component.text("%${args[0]}%"),
                        null,
                        null,
                        null
                    )
                    return Function {
                        Placeholders.parseText(comp, PlaceholderContext.of(it.fabricPlayer)).string
                    }
                }

                override fun getRequiredArgsLength(): Int = 1
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()
}