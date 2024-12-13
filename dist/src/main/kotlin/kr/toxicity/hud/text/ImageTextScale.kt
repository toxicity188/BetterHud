package kr.toxicity.hud.text

import kr.toxicity.hud.location.PixelLocation
import kotlin.math.roundToInt

class ImageTextScale(
    val name: String,
    val fileName: String,
    val location: PixelLocation,
    val ascent: Int,
    val width: Double,
    val height: Double
) {
    operator fun times(double: Double) = ImageTextScale(
        name,
        fileName,
        location,
        ascent,
        width * double,
        height * double
    )
    val normalizedHeight
        get() = height.roundToInt()

    val normalizedWidth
        get() = (width / height * normalizedHeight).roundToInt()
}