package kr.toxicity.hud.text

import kr.toxicity.hud.renderer.BackgroundRenderer
import net.kyori.adventure.key.Key

data class BackgroundKey(
    val key: Key,
    val background: BackgroundRenderer?
)