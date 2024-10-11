package kr.toxicity.hud.image

import kr.toxicity.hud.api.yaml.YamlObject

data class ImageLocation(val x: Int, val y: Int) : Comparable<ImageLocation> {
    companion object {
        private val comparator = Comparator.comparingInt { image: ImageLocation ->
            image.x
        }.thenComparingInt { image: ImageLocation ->
            image.y
        }

        val hotBarHeight = ImageLocation(0, -54)
        val zero = ImageLocation(0, 0)
    }
    constructor(section: YamlObject): this(
        section.getAsInt("x", 0),
        section.getAsInt("y", 0)
    )
    operator fun plus(other: ImageLocation) = ImageLocation(x + other.x, y + other.y)
    override fun compareTo(other: ImageLocation): Int = comparator.compare(this, other)
}