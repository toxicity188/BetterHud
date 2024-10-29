package kr.toxicity.hud.background

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.image.LoadedImage

@Deprecated(message = "Rewrite new background.")
class LegacyHudBackground(
    override val path: String,
    val name: String,

    val left: LoadedImage,
    val right: LoadedImage,
    val body: LoadedImage,

    val location: PixelLocation,
) : HudConfiguration