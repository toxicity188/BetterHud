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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import kotlin.math.abs

fun createAdventureKey(value: String) = Key.key(NAME_SPACE_ENCODED, value)

const val TEXT_SPACE_KEY_CODEPOINT = 0xC0000

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
val NEW_LAYER
    get() = if (BOOTSTRAP.useLegacyFont()) EMPTY_WIDTH_COMPONENT else WidthComponent(Component.text().content(0xC0000.parseChar()).font(SPACE_KEY), 0)

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
    private val chain: (Component) -> Unit
) {
    private var builder = Component.text()
    var isClean = true
        private set
    fun accept(block: TextComponent.Builder.() -> Unit = {}) {
        val build = builder.apply(block).build()
        builder = Component.text()
        isClean = true
        chain(build)
    }
    fun build(block: TextComponent.Builder.() -> Unit = {}): Component {
        val build = builder.apply(block).build()
        builder = Component.text()
        isClean = true
        return build
    }
    fun append(like: ComponentLike) {
        builder.append(like)
        isClean = false
    }
    fun then(firstChain: (SplitBuilder, Component) -> Unit) = SplitBuilder other@ {
        firstChain(this, it)
        accept()
    }
}

fun Component.split(endWidth: Int, charWidth: (Pair<Style, Int>) -> Int?): List<Component> {
    var i = 0
    val list = ArrayList<Component>()
    val topBuilder = SplitBuilder {
        i = 0
        list.add(it)
    }
    fun Component.parse(target: SplitBuilder) {
        if (this is TextComponent) {
            val style = style()
            if (style.font() == null) {
                val subBuilder = target.then { b, c ->
                    b.append(c)
                }
                val sb = StringBuilder()
                for (codepoint in content().codePoints()) {
                    sb.appendCodePoint(codepoint)
                    i += if (codepoint == ' '.code) 4 else charWidth(style to codepoint) ?: continue
                    if (i >= endWidth) {
                        subBuilder.accept {
                            style(style).content(sb.toString())
                        }
                        sb.setLength(0)
                    }
                }
                if (!subBuilder.isClean || sb.isNotEmpty()) target.append(subBuilder.build {
                    style(style).content(sb.toString())
                })
                for (child in children()) {
                    child.parse(subBuilder)
                }
                if (!subBuilder.isClean) target.append(subBuilder.build {
                    style(style)
                })
            } else {
                target.append(Component.text().content(content()).style(style))
            }
        }
    }
    parse(topBuilder)
    if (!topBuilder.isClean) list.add(topBuilder.build())
    return list
}