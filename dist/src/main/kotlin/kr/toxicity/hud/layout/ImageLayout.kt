package kr.toxicity.hud.layout

import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.placeholder.ConditionBuilder

class ImageLayout(
    val image: HudImage,
    val location: ImageLocation,
    val scale: Double,
    val outline: Boolean,
    val layer: Int,
    val conditions: ConditionBuilder
)