package kr.toxicity.hud.image.enums

import java.awt.image.BufferedImage

enum class FlipType {
    X {
        override fun flip(flipLocation: FlipLocation) {
            flipLocation.flipX()
        }
    },
    Y {
        override fun flip(flipLocation: FlipLocation) {
            flipLocation.flipY()
        }
    }
    ;

    abstract fun flip(flipLocation: FlipLocation)

    companion object {
        fun flip(image: BufferedImage, types: Set<FlipType>) = if (types.isEmpty()) image else BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB).apply {
            for (h in 0..<height) {
                for (w in 0..<width) {
                    FlipLocation(this, w, h).apply {
                        for (flipType in types) {
                            flipType.flip(this)
                        }
                    }.set(image.getRGB(w, h))
                }
            }
        }
    }

    class FlipLocation(
        private val image: BufferedImage,
        private var x: Int,
        private var y: Int
    ) {
        fun flipX() {
            y = image.height - 1 - y
        }
        fun flipY() {
            x = image.width - 1 - x
        }
        fun set(argb: Int) {
            image.setRGB(x, y, argb)
        }
    }
}