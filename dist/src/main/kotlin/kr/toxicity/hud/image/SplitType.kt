package kr.toxicity.hud.image

import kr.toxicity.hud.util.removeEmptySide
import kr.toxicity.hud.util.removeEmptyWidth
import java.awt.AlphaComposite
import java.awt.image.BufferedImage

enum class SplitType {
    LEFT {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                target.getSubimage(0, 0, (it.toDouble() / split * target.width).toInt().coerceAtLeast(1), target.height)
            }
        }
    },
    RIGHT {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                val getWidth = (it.toDouble() / split * target.width).toInt().coerceAtLeast(1)
                target.getSubimage(target.width - getWidth, 0, getWidth, target.height)
            }
        }
    },
    UP {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                val getHeight = (it.toDouble() / split * target.height).toInt().coerceAtLeast(1)
                BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        drawImage(target.getSubimage(0, target.height - getHeight, target.width, getHeight), 0, target.height - getHeight, null)
                        dispose()
                    }
                }.removeEmptyWidth() ?: throw RuntimeException()
            }
        }
    },
    DOWN {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        drawImage(target.getSubimage(0, 0, target.width, (it.toDouble() / split * target.height).toInt().coerceAtLeast(1)), 0, 0, null)
                        dispose()
                    }
                }.removeEmptyWidth() ?: throw RuntimeException()
            }
        }
    },
    ;
    abstract fun split(target: BufferedImage, split: Int): List<BufferedImage>
}