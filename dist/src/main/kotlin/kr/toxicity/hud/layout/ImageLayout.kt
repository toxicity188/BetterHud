package kr.toxicity.hud.layout

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.HudImage

class ImageLayout(
    val image: HudImage,
    val x: Int,
    val y: Int,
    val scale: Double,
    val outline: Boolean,
    val conditions: (HudPlayer) -> Boolean
)