package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toTextColor
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

interface ImageLayout : HudLayout<ImageElement> {
    val color: TextColor
    val scale: Double
    val space: Int
    val stack: PlaceholderBuilder<*>?
    val maxStack: PlaceholderBuilder<*>?

    class Impl(
        override val source: ImageElement,
        group: LayoutGroup,
        yamlObject: YamlObject,
        loc: PixelLocation,
    ) : ImageLayout, HudLayout<ImageElement> by HudLayout.Impl(source, group, loc, yamlObject) {
        constructor(
            s: String,
            group: LayoutGroup,
            yamlObject: YamlObject,
            loc: PixelLocation,
        ): this(
            yamlObject["name"]?.asString().ifNull("name value not set: $s").let { n ->
                ImageManager.getImage(n).ifNull("this image doesn't exist: $n")
            },
            group,
            yamlObject,
            loc
        )
        override val color: TextColor = yamlObject["color"]?.asString()?.toTextColor() ?: NamedTextColor.WHITE
        override val scale: Double = yamlObject.getAsDouble("scale", 1.0)
        override val space: Int = yamlObject.getAsInt("space", 1)
        override val stack: PlaceholderBuilder<*>? = yamlObject["stack"]?.asString()?.let {
            PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
                if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
            }
        }
        override val maxStack: PlaceholderBuilder<*>? = yamlObject["max-stack"]?.asString()?.let {
            PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
                if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
            }
        }
    }
}