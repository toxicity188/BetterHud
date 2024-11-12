package kr.toxicity.hud.text

import kr.toxicity.hud.location.PixelLocation

class ImageCharWidth(
    val name: String,
    val fileName: String,
    val location: PixelLocation,
    width: Int,
    height: Int
) : CharWidth(width, height)