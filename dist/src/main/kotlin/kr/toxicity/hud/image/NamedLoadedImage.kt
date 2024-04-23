package kr.toxicity.hud.image

import kr.toxicity.hud.util.encodeFile

class NamedLoadedImage(
    name: String,
    val image: LoadedImage
) {
    val name = "image_$name".encodeFile()
}