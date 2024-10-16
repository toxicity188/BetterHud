package kr.toxicity.hud.background

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LoadedImage

class HudBackground(
    override val path: String,
    val name: String,

    val left: LoadedImage,
    val right: LoadedImage,
    val body: LoadedImage,

    val location: ImageLocation,
) : HudConfiguration