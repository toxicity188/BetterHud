package kr.toxicity.hud.layout

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageLocation

class ImageLayout(
    val image: HudImage,
    val location: ImageLocation,
    val scale: Double,
    val outline: Boolean,
    val layer: Int,
    val conditions: (HudPlayer) -> Boolean
)