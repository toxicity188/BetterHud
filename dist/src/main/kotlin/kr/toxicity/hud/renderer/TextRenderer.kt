package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.text.DecimalFormat
import java.util.EnumMap
import java.util.LinkedList
import java.util.regex.Pattern

class TextRenderer(
    private val widthMap: Map<Char, Int>,
    private val defaultColor: TextColor,
    private val data: HudTextData,
    pattern: String,
    private val align: LayoutAlign,
    private val scale: Double,
    private val x: Int,

    private val deserializeText: Boolean,

    space: Int,

    private val numberEquation: TEquation,
    private val numberPattern: DecimalFormat,

    private val condition: ConditionBuilder
) {
    companion object {
        private val decimalPattern = Pattern.compile("([0-9]+((\\.([0-9]+))?))")
        private val componentPattern = Pattern.compile("<(?<name>(([a-zA-Z]|#|[0-9]|/)+))((:(?<argument>([a-zA-Z]|[0-9]|:|,)+))?)>")

        private val formats: MutableMap<String, (List<String>, ComponentStyleBuilder) -> Unit> = HashMap()

        private fun addFormat(name: List<String>, function: (List<String>, ComponentStyleBuilder) -> Unit) {
            name.forEach {
                formats[it] = function
            }
        }

        private fun getFormat(name: String): ((List<String>, ComponentStyleBuilder) -> Unit)? {
            return if (name.startsWith('#') && name.length == 7) {
                TextColor.fromHexString(name)?.let {
                    { _, builder ->
                        builder.color = it
                    }
                }
            } else formats[name]
        }

        init {
            NamedTextColor.NAMES.keyToValue().forEach {
                addFormat(listOf(it.key)) { _, builder ->
                    builder.color = it.value
                }
                addFormat(listOf("/${it.key}")) { _, builder ->
                    if (builder.color == it.value) builder.color = NamedTextColor.WHITE
                }
            }
            fun addDecoration(names: List<String>, decoration: TextDecoration) {
                addFormat(names) { args, format ->
                    format.decoration[decoration] = if (args.isEmpty()) TextDecoration.State.TRUE else when (args[0]) {
                        "false" -> TextDecoration.State.FALSE
                        "true" -> TextDecoration.State.TRUE
                        else -> TextDecoration.State.NOT_SET
                    }
                }
                addFormat(names.map {
                    "/$it"
                }) { _, format ->
                    format.decoration[decoration] = TextDecoration.State.FALSE
                }
            }
            addDecoration(listOf("bold", "b"), TextDecoration.BOLD)
            addDecoration(listOf("italic", "em", "i"), TextDecoration.ITALIC)
        }
    }

    private val sComponent = space.toSpaceComponent()
    private val spaceComponent = 4.toSpaceComponent()

    private val patternMapper = parseStyle(pattern) {
        it.build()
    }

    private fun <T> parseStyle(target: String, mapper: (ComponentStyleBuilder) -> T): List<T> {
        val format = ArrayList<ComponentFormat>()
        val matcher = componentPattern.matcher(target)
        while (matcher.find()) format.add(
            ComponentFormat(
                matcher.group("name"),
                matcher.group("argument")?.split(':') ?: emptyList()
            )
        )
        val strings = target.split(componentPattern).map {
            ComponentStyleBuilder(it)
        }
        for ((i, componentFormat) in format.withIndex()) {
            val t = i + 1
            if (t > strings.lastIndex) break
            if (componentFormat.name == "image" && componentFormat.args.isNotEmpty()) {
                data.images[componentFormat.args[0]]?.let {
                    strings[t].images.add(it)
                }
            } else getFormat(componentFormat.name)?.let {
                strings.subList(t, strings.size).forEach { str ->
                    it(componentFormat.args, str)
                }
            }
        }
        return strings.map {
            mapper(it)
        }
    }

    private class ComponentFormat(
        val name: String,
        val args: List<String>
    )
    private inner class ComponentStyleBuilder(
        val pattern: String
    ) {
        var color: TextColor = defaultColor
        var decoration = EnumMap(TextDecoration.entries.associateWith {
            TextDecoration.State.FALSE
        })
        val images = ArrayList<WidthComponent>()

        fun build() = ComponentStyle(
            PlaceholderManagerImpl.parse(pattern),
            images,
            Style.style()
                .color(color)
                .decorations(decoration)
                .font(data.word)
                .build(),
            run {
                var i = 1
                if (decoration[TextDecoration.BOLD] == TextDecoration.State.TRUE) i++
                if (decoration[TextDecoration.ITALIC] == TextDecoration.State.TRUE) i++
                i
            }
        )

        fun buildRaw() = RawComponentStyle(
            pattern,
            images,
            Style.style()
                .decorations(decoration)
                .color(color)
                .font(data.word)
                .build(),
            run {
                var i = 1
                if (decoration[TextDecoration.BOLD] == TextDecoration.State.TRUE) i++
                if (decoration[TextDecoration.ITALIC] == TextDecoration.State.TRUE) i++
                i
            }
        )
    }
    private class ComponentStyle(
        val pattern: (UpdateEvent) -> (HudPlayer) -> String,
        val images: List<WidthComponent>,
        val style: Style,
        val multiply: Int
    ) {
        fun map(updateEvent: UpdateEvent) = MappedComponentStyle(
            pattern(updateEvent),
            images,
            style,
            multiply
        )
    }
    private class MappedComponentStyle(
        val value: (HudPlayer) -> String,
        val images: List<WidthComponent>,
        val style: Style,
        val multiply: Int
    )
    private class RawComponentStyle(
        val value: String,
        val images: List<WidthComponent>,
        val style: Style,
        val multiply: Int
    )


    fun getText(reason: UpdateEvent): (HudPlayer) -> PixelComponent {
        val patternMap = patternMapper.map {
            it.map(reason)
        }
        val cond = condition.build(reason)

        return build@ { player ->
            if (!cond(player)) return@build EMPTY_PIXEL_COMPONENT
            var comp = EMPTY_WIDTH_COMPONENT

            fun applyString(targetString: String, style: Style, multiply: Int, images: List<WidthComponent>) {
                images.forEach {
                    comp += it
                }
                var original = targetString
                if (original == "") return
                val matcher = decimalPattern.matcher(original)
                val number = LinkedList<String>()
                while (matcher.find()) {
                    number.add(numberPattern.format(numberEquation.evaluate(matcher.group().toDouble())))
                }
                if (number.isNotEmpty()) {
                    val sb = StringBuilder()
                    original.split(decimalPattern).forEach {
                        sb.append(it)
                        number.poll()?.let { n ->
                            sb.append(n)
                        }
                    }
                    original = sb.toString()
                }
                original.forEach { char ->
                    if (char == ' ') {
                        comp += spaceComponent
                    } else {
                        widthMap[char]?.let { width ->
                            comp += WidthComponent(Component.text().content(char.toString()).style(style), Math.round(width.toDouble() * scale).toInt() + multiply) + sComponent
                        }
                    }
                }
            }
            for (mappedComponentStyle in patternMap) {
                val target = mappedComponentStyle.value(player)
                if (deserializeText) {
                    parseStyle(target) {
                        it.buildRaw()
                    }.forEach {
                        applyString(it.value, it.style, it.multiply, it.images)
                    }
                } else applyString(target, mappedComponentStyle.style, mappedComponentStyle.multiply, mappedComponentStyle.images)
            }
            data.background?.let {
                val builder = Component.text().append(it.left.component)
                var length = 0
                while (length < comp.width) {
                    builder.append(it.body.component)
                    length += it.body.width
                }
                val total = it.left.width + length + it.right.width
                val minus = -total + (length - comp.width) / 2 + it.left.width
                comp = WidthComponent(builder.append(it.right.component), total) + minus.toSpaceComponent() + comp
            }
            comp.toPixelComponent(when (align) {
                LayoutAlign.LEFT -> x
                LayoutAlign.CENTER -> x - comp.width / 2
                LayoutAlign.RIGHT -> x - comp.width
            })
        }
    }
}