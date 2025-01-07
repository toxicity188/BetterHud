package kr.toxicity.hud.util

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.manager.ConfigManagerImpl
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.abs
import kotlin.math.roundToInt

fun createAdventureKey(value: String) = Key.key(NAME_SPACE_ENCODED, value)

const val TEXT_SPACE_KEY_CODEPOINT = 0xC0000
const val TEXT_IMAGE_START_CODEPOINT = 0xC0000

val CONSOLE
    get() = BOOTSTRAP.consoleSource()

val SPACE_KEY
    get() = ConfigManagerImpl.key.spaceKey
val DEFAULT_KEY
    get() = ConfigManagerImpl.key.defaultKey

val DEFAULT_TEXT_DECORATION = TextDecoration.entries.associateWith {
    TextDecoration.State.FALSE
}

fun String.toLegacySerializer() = when (this) {
    "ampersand" -> LEGACY_AMPERSAND
    "section" -> LEGACY_SECTION
    "both" -> LEGACY_BOTH
    else -> throw RuntimeException("Unknown legacy serializer: $this")
}

fun String.toComponent() = Component.text(this).color(NamedTextColor.WHITE).decorations(DEFAULT_TEXT_DECORATION)

val EMPTY_COMPONENT: Component = Component.empty()
val EMPTY_WIDTH_COMPONENT
    get() = WidthComponent(Component.text(), 0)
val EMPTY_PIXEL_COMPONENT
    get() = PixelComponent(EMPTY_WIDTH_COMPONENT, 0)

const val CENTER_SPACE_CODEPOINT = 0xD0000

val NEGATIVE_ONE_SPACE_COMPONENT
    get() = WidthComponent(Component.text().content((CENTER_SPACE_CODEPOINT - 1).parseChar()), 0)

fun String.toTextColor() = if (startsWith('#') && length == 7) {
    TextColor.fromHexString(this)
} else NamedTextColor.NAMES.value(this) ?: NamedTextColor.WHITE
fun WidthComponent.toPixelComponent(pixel: Int) = PixelComponent(this, pixel)

fun PixelComponent.append(space: Int, other: PixelComponent): PixelComponent {
    var comp = EMPTY_WIDTH_COMPONENT + component
    if (space != 1) {
        comp += (space - 1).toSpaceComponent()
    }
    return PixelComponent(
        comp + other.component,
        if (abs(pixel) > abs(other.pixel)) pixel else other.pixel
    )
}

fun Int.parseChar(): String {
    return if (this <= 0xFFFF) this.toChar().toString()
    else {
        val t = this - 0x10000
        return "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
    }
}

fun Int.toSpaceComponent() = toSpaceComponent(this)
fun Int.toSpaceComponent(width: Int) = WidthComponent(
    Component.text()
        .content((this + 0xD0000).parseChar()),
    width
)

fun WidthComponent.finalizeFont() = apply {
    component.font(SPACE_KEY)
}

private class SplitBuilder(
    private val chain: (TextComponent.Builder) -> Unit
) {
    private var builder = Component.text()
    var isClean = true
        private set
    fun accept(block: TextComponent.Builder.() -> Unit = {}) {
        val build = if (isClean) builder.apply(block) else builder.append(Component.text().apply(block))
        builder = Component.text()
        isClean = true
        chain(build)
    }
    fun build(block: TextComponent.Builder.() -> Unit = {}): TextComponent.Builder {
        val build = if (isClean) builder.apply(block) else builder.append(Component.text().apply(block))
        builder = Component.text()
        isClean = true
        return build
    }
    fun style(style: Style) {
        builder.style(style)
    }
    fun append(like: ComponentLike) {
        builder.append(like)
        isClean = false
    }
    fun then(firstChain: (SplitBuilder, TextComponent.Builder) -> Unit) = SplitBuilder other@ {
        firstChain(this, it)
        accept()
    }
}

private fun Style.parseDecoration(decoration: TextDecoration, default: Boolean): Boolean = when (decoration(decoration)) {
    TextDecoration.State.NOT_SET -> default
    TextDecoration.State.FALSE -> false
    TextDecoration.State.TRUE -> true
}

data class SplitOption(
    val endWidth: Int,
    val space: Int,
    val forceSplit: Boolean
)

private val COMBINE = sequenceOf(
    Character.NON_SPACING_MARK,
    Character.COMBINING_SPACING_MARK,
).map {
    it.toInt()
}.toSet()

fun Component.split(option: SplitOption, charWidth: (Pair<Style, Int>) -> Int?): List<WidthComponent> {
    var i = 0
    val list = ArrayList<WidthComponent>()
    val topBuilder = SplitBuilder {
        list += WidthComponent(it, i)
        i = 0
    }
    val shouldApplySpace = option.space > 0
    fun Component.parse(target: SplitBuilder, bold: Boolean, italic: Boolean) {
        if (this is TextComponent) {
            var style = style()
            var add = 0
            val subBold = style.parseDecoration(TextDecoration.BOLD, bold)
            val subItalic = style.parseDecoration(TextDecoration.ITALIC, italic)
            style = style.decorations(mapOf(
                TextDecoration.BOLD to if (subBold) TextDecoration.State.TRUE else TextDecoration.State.FALSE,
                TextDecoration.ITALIC to if (subItalic) TextDecoration.State.TRUE else TextDecoration.State.FALSE
            ))
            if (subBold) add++
            if (subItalic) add++
            val subBuilder = target.then { b, c ->
                b.append(c)
            }
            val sb = StringBuilder()

            subBuilder.style(style)
            fun end() {
                subBuilder.accept {
                    content(sb.toString())
                }
                sb.setLength(0)
            }
            fun add(component: ComponentLike) {
                subBuilder.append(Component.text().content(sb.toString()))
                subBuilder.append(component)
                sb.setLength(0)
            }
            for (codepoint in content().codePoints()) {
                if ('\n'.code == codepoint) {
                    end()
                    continue
                }
                val length = (if (codepoint == ' '.code) 4 else charWidth(style to codepoint) ?: continue) + add + (if (shouldApplySpace) option.space + add else 0)
                val shouldCombine = COMBINE.contains(Character.getType(codepoint))
                if (shouldCombine) {
                    add((-length).toSpaceComponent().finalizeFont().component)
                } else i += length
                sb.appendCodePoint(codepoint)
                if (shouldApplySpace) {
                    sb.appendCodePoint(TEXT_SPACE_KEY_CODEPOINT)
                }
                if (i >= option.endWidth && (i >= (1.25 * option.endWidth).roundToInt() || ' '.code == codepoint) || option.forceSplit) end()
            }
            if (!subBuilder.isClean || sb.isNotEmpty()) target.append(subBuilder.build {
                content(sb.toString())
            })
            for (child in children()) {
                child.parse(subBuilder, subBold, subItalic)
            }
            if (!subBuilder.isClean) target.append(subBuilder.build {
                style(style)
            })
        }
    }
    val style = style()
    parse(topBuilder, style.parseDecoration(TextDecoration.BOLD, false), style.parseDecoration(TextDecoration.ITALIC, false))
    if (!topBuilder.isClean) list += WidthComponent(topBuilder.build(), i)
    return list
}

infix fun PixelComponent.applyColor(color: TextColor?): PixelComponent = if (color == null) this else PixelComponent(
    component.applyColor(color),
    pixel
)

infix fun WidthComponent.applyColor(color: TextColor?): WidthComponent = when (color?.value()) {
    null -> this
    NamedTextColor.WHITE.value() -> WidthComponent(
        component.build().toBuilder().color(null),
        width
    )
    else -> {
        val build = component.build()
        val finalColor = build.color()?.let {
            it * color
        } ?: color
        WidthComponent(
            build.toBuilder().color(finalColor),
            width
        )
    }
}

operator fun TextColor.times(other: TextColor): TextColor {
    infix fun Int.process(other: Int) = (toDouble() / 255 * other.toDouble()).toInt()
    return TextColor.color(
        red() process other.red(),
        green() process other.green(),
        blue() process other.blue()
    )
}