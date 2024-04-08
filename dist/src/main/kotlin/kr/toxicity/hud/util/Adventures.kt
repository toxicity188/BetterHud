package kr.toxicity.hud.util

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.abs

val SPACE_KEY = Key.key("$NAME_SPACE:space")
val LEGACY_SPACE_KEY = Key.key("$NAME_SPACE:legacy_space")

val DEFAULT_TEXT_DECORATION = TextDecoration.entries.associateWith {
    TextDecoration.State.FALSE
}

fun String.toComponent() = Component.text(this).color(NamedTextColor.WHITE).decorations(DEFAULT_TEXT_DECORATION)

val EMPTY_COMPONENT: Component = Component.empty()
val EMPTY_WIDTH_COMPONENT
    get() = WidthComponent(Component.text().color(NamedTextColor.WHITE), 0)
val EMPTY_PIXEL_COMPONENT
    get() = PixelComponent(EMPTY_WIDTH_COMPONENT, 0)
val NEW_LAYER
    get() = if (VERSION.version <= 18) EMPTY_WIDTH_COMPONENT else WidthComponent(Component.text().content(0xC0000.parseChar()).font(SPACE_KEY), 0)

private val LEGACY_NEGATIVE_ONE_SPACE_COMPONENT = WidthComponent(Component.text().content((0xFFC00 - 1).parseChar()).font(LEGACY_SPACE_KEY), 0)
private val CURRENT_NEGATIVE_ONE_SPACE_COMPONENT = WidthComponent(Component.text().content((0xD0000 - 1).parseChar()).font(SPACE_KEY), 0)
val NEGATIVE_ONE_SPACE_COMPONENT
    get() = if (VERSION.version <= 18) {
        LEGACY_NEGATIVE_ONE_SPACE_COMPONENT
    } else {
        CURRENT_NEGATIVE_ONE_SPACE_COMPONENT
    }

fun String.toTextColor() = if (startsWith('#') && length == 7) {
    TextColor.fromHexString(this)
} else NamedTextColor.NAMES.value(this) ?: NamedTextColor.WHITE
fun WidthComponent.toPixelComponent(pixel: Int) = PixelComponent(this, pixel)

fun Int.parseChar(): String {
    return if (this <= 0xFFFF) this.toChar().toString()
    else {
        val t = this - 0x10000
        return "${((t ushr 10) + 0xD800).toChar()}${((t and 1023) + 0xDC00).toChar()}"
    }
}

fun Int.toSpaceComponent() = toSpaceComponent(this)
fun Int.toSpaceComponent(width: Int): WidthComponent {
    val builder = Component.text()
    return if (VERSION.version <= 18) {
        val abs = abs(this)
        if (abs > 256) {
            val i = (if (this > 0) 1 else -1)
            WidthComponent(
                builder.font(LEGACY_SPACE_KEY)
                    .append(Component.text(((abs / 256 + 255) * i + 0xFFC00).parseChar()))
                    .append(Component.text(((abs % 256) * i + 0xFFC00).parseChar())),
                width
            )
        } else WidthComponent(builder.font(LEGACY_SPACE_KEY).content((this + 0xFFC00).parseChar()), width)
    } else {
        WidthComponent(builder.font(SPACE_KEY).content((this + 0xD0000).parseChar()), width)
    }
}