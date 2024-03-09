package kr.toxicity.hud.image

import kr.toxicity.hud.util.removeEmptyWidth
import java.awt.AlphaComposite
import java.awt.image.BufferedImage

enum class SplitType {
    LEFT {
        override fun split(name: String, target: BufferedImage, split: Int): List<NamedLoadedImage> {
            return (1..split).map {
                NamedLoadedImage(
                    "${name}_$it.png",
                    LoadedImage(
                        target.getSubimage(0, 0, (it.toDouble() / split * target.width).toInt().coerceAtLeast(1), target.height),
                        0,
                        0
                    )
                )
            }
        }
    },
    RIGHT {
        override fun split(name: String, target: BufferedImage, split: Int): List<NamedLoadedImage> {
            return (1..split).map {
                val getWidth = (it.toDouble() / split * target.width).toInt().coerceAtLeast(1)
                val xOffset = target.width - getWidth
                NamedLoadedImage(
                    "${name}_$it.png",
                    LoadedImage(
                        target.getSubimage(xOffset, 0, getWidth, target.height),
                        xOffset,
                        0
                    )
                )
            }
        }
    },
    UP {
        override fun split(name: String, target: BufferedImage, split: Int): List<NamedLoadedImage> {
            return (1..split).map {
                val getHeight = (it.toDouble() / split * target.height).toInt().coerceAtLeast(1)
                NamedLoadedImage(
                    "${name}_$it.png",
                    BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                        createGraphics().run {
                            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                            drawImage(target.getSubimage(0, target.height - getHeight, target.width, getHeight), 0, target.height - getHeight, null)
                            dispose()
                        }
                    }.removeEmptyWidth() ?: throw RuntimeException()
                )
            }
        }
    },
    DOWN {
        override fun split(name: String, target: BufferedImage, split: Int): List<NamedLoadedImage> {
            return (1..split).map {
                NamedLoadedImage(
                    "${name}_$it.png",
                    BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                        createGraphics().run {
                            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                            drawImage(target.getSubimage(0, 0, target.width, (it.toDouble() / split * target.height).toInt().coerceAtLeast(1)), 0, 0, null)
                            dispose()
                        }
                    }.removeEmptyWidth() ?: throw RuntimeException()
                )
            }
        }
    },
    ;
    abstract fun split(name: String, target: BufferedImage, split: Int): List<NamedLoadedImage>
}