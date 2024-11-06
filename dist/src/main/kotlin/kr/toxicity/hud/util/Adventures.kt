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
    get() = BOOTSTRAP.console()

val SPACE_KEY
    get() = ConfigManagerImpl.key.spaceKey
val LEGACY_SPACE_KEY
    get() = ConfigManagerImpl.key.legacySpaceKey
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
    get() = WidthComponent(Component.text().color(NamedTextColor.WHITE), 0)
val EMPTY_PIXEL_COMPONENT
    get() = PixelComponent(EMPTY_WIDTH_COMPONENT, 0)

const val LEGACY_CENTER_SPACE_CODEPOINT = 0xFFC00
const val CURRENT_CENTER_SPACE_CODEPOINT = 0xD0000

private val LEGACY_NEGATIVE_ONE_SPACE_COMPONENT
    get() = WidthComponent(Component.text().content((LEGACY_CENTER_SPACE_CODEPOINT - 1).parseChar()).font(LEGACY_SPACE_KEY), 0)
private val CURRENT_NEGATIVE_ONE_SPACE_COMPONENT
    get() = WidthComponent(Component.text().content((CURRENT_CENTER_SPACE_CODEPOINT - 1).parseChar()).font(SPACE_KEY), 0)
val NEGATIVE_ONE_SPACE_COMPONENT
    get() = if (BOOTSTRAP.useLegacyFont()) {
        LEGACY_NEGATIVE_ONE_SPACE_COMPONENT
    } else {
        CURRENT_NEGATIVE_ONE_SPACE_COMPONENT
    }

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
fun Int.toSpaceComponent(width: Int) = if (BOOTSTRAP.useLegacyFont()) {
    val abs = abs(this)
    if (abs > 256) {
        val i = if (this > 0) 1 else -1
        WidthComponent(
            Component.text()
                .font(LEGACY_SPACE_KEY)
                .content("${((abs / 256 + 255) * i + 0xFFC00).parseChar()}${(abs % 256 * i + 0xFFC00).parseChar()}"),
            width
        )
    } else WidthComponent(
        Component.text()
            .font(LEGACY_SPACE_KEY)
            .content((this + 0xFFC00).parseChar()),
        width
    )
} else {
    WidthComponent(
        Component.text()
            .font(SPACE_KEY)
            .content((this + 0xD0000).parseChar()),
        width
    )
}

private class SplitBuilder(
    private val chain: (TextComponent.Builder) -> Unit
) {
    private var builder = Component.text()
    var isClean = true
        private set
    fun accept(block: TextComponent.Builder.() -> Unit = {}) {
        val build = builder.apply(block)
        builder = Component.text()
        isClean = true
        chain(build)
    }
    fun build(block: TextComponent.Builder.() -> Unit = {}): TextComponent.Builder {
        val build = builder.apply(block)
        builder = Component.text()
        isClean = true
        return build
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

fun Component.split(endWidth: Int, space: Int, charWidth: (Pair<Style, Int>) -> Int?): List<WidthComponent> {
    var i = 0
    val list = ArrayList<WidthComponent>()
    val topBuilder = SplitBuilder {
        list.add(WidthComponent(it, i))
        i = 0
    }
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

            fun end() {
                subBuilder.accept {
                    style(style).content(sb.toString())
                }
                sb.setLength(0)
            }
            for (codepoint in content().codePoints()) {
                if ('\n'.code == codepoint) {
                    end()
                    continue
                }
                sb.appendCodePoint(codepoint)
                i += if (codepoint == ' '.code) 4 else charWidth(style to codepoint) ?: continue
                i += add
                if (space > 0) {
                    if (BOOTSTRAP.useLegacyFont()) subBuilder.append(space.toSpaceComponent().component)
                    else sb.appendCodePoint(TEXT_SPACE_KEY_CODEPOINT)
                    i += space + add
                }
                if (i >= endWidth && (i >= (1.25 * endWidth).roundToInt() || ' '.code == codepoint)) end()
            }
            if (!subBuilder.isClean || sb.isNotEmpty()) target.append(subBuilder.build {
                style(style).content(sb.toString())
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
    if (!topBuilder.isClean) list.add(WidthComponent(topBuilder.build(), i))
    return list
}