package kr.toxicity.hud.layout

import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.player.HudHead

class HeadLayout(
    val head: HudHead,
    val location: ImageLocation,
    val outline: Boolean,
    val layer: Int,
    val conditions: ConditionBuilder
)