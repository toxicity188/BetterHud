package kr.toxicity.hud.bootstrap.fabric.compatibility

import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.parsers.ParserBuilder
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.fabric.util.fabricPlayer
import kr.toxicity.hud.bootstrap.fabric.util.toMiniMessageString
import java.util.function.Function

class TextPlaceholderAPICompatibility : Compatibility {

    private val builder by lazy {
        ParserBuilder.of()
            .serverPlaceholders()
            .build()
    }

    override val website: String = "https://modrinth.com/mod/placeholder-api"

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "parse" to HudPlaceholder.builder<String>()
                .requiredArgsLength(1)
                .function { args, _ ->
                    val node = TextNode.of("%${args[0]}%")
                    Function {
                        builder.parseComponent(node, PlaceholderContext.of(it.fabricPlayer).asParserContext()).toMiniMessageString()
                    }
                }
                .build()
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()
}