package kr.toxicity.hud.image

import java.awt.AlphaComposite
import java.awt.image.BufferedImage

enum class SplitType {
    LEFT {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                target.getSubimage(0, 0, (it.toDouble() / split * target.width).toInt(), target.height)
            }
        }
    },
    RIGHT {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                val getWidth = (it.toDouble() / split * target.width).toInt()
                target.getSubimage(target.width - getWidth, 0, getWidth, target.height)
            }
        }
    },
    UP {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                val getHeight = (it.toDouble() / split * target.height).toInt()
                BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        drawImage(target.getSubimage(0, target.height - getHeight, target.width, getHeight), 0, target.height - getHeight, null)
                        dispose()
                    }
                }
            }
        }
    },
    DOWN {
        override fun split(target: BufferedImage, split: Int): List<BufferedImage> {
            return (1..split).map {
                BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        drawImage(target.getSubimage(0, 0, target.width, (it.toDouble() / split * target.height).toInt()), 0, 0, null)
                        dispose()
                    }
                }
            }
        }
    },
    ;
    abstract fun split(target: BufferedImage, split: Int): List<BufferedImage>
}