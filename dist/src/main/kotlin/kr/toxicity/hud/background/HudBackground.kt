package kr.toxicity.hud.background

import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LoadedImage

class HudBackground(
    val name: String,

    val left: LoadedImage,
    val right: LoadedImage,
    val body: LoadedImage,

    val location: ImageLocation,
)