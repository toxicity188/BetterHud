package kr.toxicity.hud.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * A 64-bit digest of a component tree: text, style and shape, in order.
 * It is used to notice that a HUD frame is the same as the previous one without comparing the trees deeply.
 */
fun Component.frameDigest(): Long {
    var hash = digest(this, 0x243F6A8885A308D3L)
    hash = (hash xor (hash ushr 33)) * -0xae502812aa7333L
    hash = (hash xor (hash ushr 33)) * -0x3b314601e57a13adL
    return hash xor (hash ushr 33)
}

private fun digest(component: Component, seed: Long): Long {
    var hash = seed * -0x61c8864680b583ebL
    hash += if (component is TextComponent) component.content().hashCode().toLong() else component.hashCode().toLong()
    hash = hash * 0x100000001B3L + component.style().hashCode()
    val children = component.children()
    hash = hash * 0x100000001B3L + children.size
    for (child in children) hash = digest(child, hash)
    return hash
}
