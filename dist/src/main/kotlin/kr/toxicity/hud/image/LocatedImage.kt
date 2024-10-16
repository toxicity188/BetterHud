package kr.toxicity.hud.image

import kr.toxicity.hud.location.PixelLocation

class LocatedImage(
    val image: LoadedImage,
    val location: PixelLocation,
    val scale: Double
)