package kr.toxicity.hud.background

import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.removeEmptySide
import kr.toxicity.hud.util.removeEmptyWidth
import java.awt.image.BufferedImage

//TODO Fix this
class BackgroundImage(
    val first: LoadedImage,
    val second: LoadedImage,
    val third: LoadedImage
) {
    companion object {
        fun splitOf(line: Int, image: BufferedImage): List<BackgroundImage> {
            if (line < 1) throw RuntimeException("line cannot be < 1")
            val finalLine = line.coerceAtMost(3)
            val load = image.removeEmptySide().ifNull("Empty image.").image

            val width = load.width
            val height = load.height

            val widthSplit = (width.toDouble() / 3.0).toInt()
            val heightSplit = (height.toDouble() / finalLine.toDouble()).toInt()

            val widthMod = width % widthSplit
            val heightMod = height % heightSplit

            return listOf()
        }
    }
}