package kr.toxicity.hud.text

import kr.toxicity.hud.location.PixelLocation

class ImageCharWidth(
    val name: String,
    val fileName: String,
    val location: PixelLocation,
    val scale: Double,
    width: Int,
    height: Int
) : CharWidth(width, height) {
    override fun scaledWidth(scale: Double) = super.scaledWidth(scale * this.scale)
}