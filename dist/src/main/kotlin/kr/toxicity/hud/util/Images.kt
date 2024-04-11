package kr.toxicity.hud.util

import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.image.NamedLoadedImage
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

fun RenderedImage.save(file: File) {
    ImageIO.write(this, "png", file)
}
fun RenderedImage.save(outputStream: OutputStream) {
    ImageIO.write(this, "png", outputStream)
}

fun File.toImage(): BufferedImage = ImageIO.read(this)
fun InputStream.toImage(): BufferedImage = ImageIO.read(this)

fun LoadedImage.toNamed(name: String) = NamedLoadedImage(name, this)

fun BufferedImage.removeEmptyWidth(): LoadedImage? {

    var widthA = 0
    var widthB = width

    for (i1 in 0..<width) {
        for (i2 in 0..<height) {
            if ((getRGB(i1, i2) and -0x1000000) ushr 24 > 0) {
                if (widthA < i1) widthA = i1
                if (widthB > i1) widthB = i1
            }
        }
    }
    val finalWidth = widthA - widthB + 1

    if (finalWidth <= 0) return null

    return LoadedImage(
        getSubimage(widthB, 0, finalWidth, height),
        widthB,
        0
    )
}
fun BufferedImage.removeEmptySide(): LoadedImage? {
    var heightA = 0
    var heightB = height

    var widthA = 0
    var widthB = width

    for (i1 in 0..<width) {
        for (i2 in 0..<height) {
            if ((getRGB(i1, i2) and -0x1000000) ushr 24 > 0) {
                if (widthA < i1) widthA = i1
                if (widthB > i1) widthB = i1
                if (heightA < i2) heightA = i2
                if (heightB > i2) heightB = i2
            }
        }
    }
    val finalWidth = widthA - widthB + 1
    val finalHeight = heightA - heightB + 1

    if (finalWidth <= 0 || finalHeight <= 0) return null

    return LoadedImage(
        getSubimage(widthB, heightB, finalWidth, finalHeight),
        widthB,
        heightB
    )
}