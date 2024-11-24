package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toTextColor
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

class ImageLayout(
    s: String,
    yamlObject: YamlObject,
    loc: PixelLocation,
) : HudLayout(loc, yamlObject) {
    val image: HudImage = yamlObject["name"]?.asString().ifNull("name value not set: $s").let { n ->
        ImageManager.getImage(n).ifNull("this image doesn't exist: $n")
    }
    val color: TextColor = yamlObject["color"]?.asString()?.toTextColor() ?: NamedTextColor.WHITE
    val scale: Double = yamlObject.getAsDouble("scale", 1.0)
    val space: Int = yamlObject.getAsInt("space", 1)
    val stack: PlaceholderBuilder<*>? = yamlObject["stack"]?.asString()?.let {
        PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
            if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
        }
    }
    val maxStack: PlaceholderBuilder<*>? = yamlObject["max-stack"]?.asString()?.let {
        PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
            if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
        }
    }
}