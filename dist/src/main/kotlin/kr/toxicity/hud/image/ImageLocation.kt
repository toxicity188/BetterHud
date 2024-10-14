package kr.toxicity.hud.image

import kr.toxicity.hud.api.yaml.YamlObject

data class ImageLocation(val x: Int, val y: Int, val opacity: Double) : Comparable<ImageLocation> {
    companion object {
        private fun Double.asColor() = coerceAtLeast(0.0).coerceAtMost(1.0)
        private val comparator = Comparator.comparingInt { image: ImageLocation ->
            image.x
        }.thenComparingInt { image: ImageLocation ->
            image.y
        }.thenComparingDouble { image: ImageLocation ->
            image.opacity
        }

        val hotBarHeight = ImageLocation(0, -54, 1.0)
        val zero = ImageLocation(0, 0, 1.0)
    }
    constructor(section: YamlObject): this(
        section.getAsInt("x", 0),
        section.getAsInt("y", 0),
        section.getAsDouble("opacity", 1.0).asColor()
    )
    operator fun plus(other: ImageLocation) = ImageLocation(x + other.x, y + other.y, (opacity * other.opacity).asColor())
    override fun compareTo(other: ImageLocation): Int = comparator.compare(this, other)
}