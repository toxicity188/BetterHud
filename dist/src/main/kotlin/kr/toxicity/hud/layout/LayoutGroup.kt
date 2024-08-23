package kr.toxicity.hud.layout

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.equation.AnimationLocation
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.NamedTextColor
import kr.toxicity.hud.api.yaml.YamlObject
import java.text.DecimalFormat

class LayoutGroup(
    override val path: String,
    sender: Audience,
    section: YamlObject
): HudConfiguration {

    private val loc = ImageLocation(section)

    val align = section.get("align")?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that align: $it") {
            LayoutAlign.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutAlign.LEFT
    val offset = section.get("offset")?.asString()?.let {
        runWithExceptionHandling(sender, "Unable to find that offset: $it") {
            LayoutOffset.valueOf(it.uppercase())
        }.getOrNull()
    } ?: LayoutOffset.CENTER

    val image: List<ImageLayout> = ArrayList<ImageLayout>().apply {
        section.get("images")?.asObject()?.forEachSubConfiguration { s, yamlObject ->
            add(
                ImageLayout(
                    yamlObject.get("name")?.asString().ifNull("name value not set: $s").let { n ->
                        ImageManager.getImage(n).ifNull("this image doesn't exist: $n")
                    },
                    yamlObject.get("color")?.asString()?.toTextColor() ?: NamedTextColor.WHITE,
                    ImageLocation(yamlObject) + loc,
                    yamlObject.getAsDouble("scale", 1.0),
                    yamlObject.getAsBoolean("outline", false),
                    yamlObject.getAsInt("layer", 0),
                    yamlObject.getAsInt("space", 1),
                    yamlObject.get("stack")?.asString()?.let {
                        PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
                            if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
                        }
                    },
                    yamlObject.get("max-stack")?.asString()?.let {
                        PlaceholderManagerImpl.find(it).ifNull("this placeholder doesn't exist: $it").apply {
                            if (clazz !=  java.lang.Number::class.java) throw RuntimeException("this placeholder is not integer: $it")
                        }
                    },
                    yamlObject.get("follow")?.asString(),
                    yamlObject.toConditions()
                )
            )
        }
    }
    val text: List<TextLayout> = ArrayList<TextLayout>().apply {
        section.get("texts")?.asObject()?.forEachSubConfiguration { s, yamlObject ->
            val scale = yamlObject.getAsDouble("scale", 1.0)
            add(
                TextLayout(
                    yamlObject.get("pattern")?.asString().ifNull("pattern value not set: $s"),
                    yamlObject.get("name")?.asString().ifNull("name value not set: $s").let { n ->
                        TextManager.getText(n).ifNull("this text doesn't exist: $n")
                    },
                    ImageLocation(yamlObject) + loc,
                    scale,
                    yamlObject.getAsInt("space", 0).coerceAtLeast(0),
                    yamlObject.get("align")?.asString().toLayoutAlign(),
                    yamlObject.get("color")?.asString()?.toTextColor() ?: NamedTextColor.WHITE,
                    yamlObject.getAsBoolean("outline", false),
                    yamlObject.getAsInt("layer", 0),
                    yamlObject.get("number-equation")?.asString()?.let {
                        TEquation(it)
                    } ?: TEquation.t,
                    yamlObject.get("number-format")?.asString()?.let {
                        DecimalFormat(it)
                    } ?: ConfigManagerImpl.numberFormat,
                    yamlObject.getAsBoolean("disable-number-format", true),
                    yamlObject.get("background")?.asString()?.let {
                        BackgroundManager.getBackground(it)
                    },
                    yamlObject.getAsDouble("background-scale", scale),
                    yamlObject.get("follow")?.asString(),
                    yamlObject.get("emoji-pixel")?.asObject()?.let {
                        ImageLocation(it)
                    } ?: ImageLocation.zero,
                    yamlObject.getAsDouble("emoji-scale", 1.0).apply {
                        if (this <= 0) throw RuntimeException("emoji-scale cannot be <= 0")
                    },
                    yamlObject.getAsBoolean("use-legacy-format", true),
                    yamlObject.get("legacy-serializer")?.asString()?.toLegacySerializer() ?: ConfigManagerImpl.legacySerializer,
                    yamlObject.toConditions()
                )
            )
        }
    }
    val head: List<HeadLayout> = ArrayList<HeadLayout>().apply {
        section.get("heads")?.asObject()?.forEachSubConfiguration { s, yamlObject ->
            add(
                HeadLayout(
                    yamlObject.get("name")?.asString().ifNull("name value not set: $s").let {
                        PlayerHeadManager.getHead(it).ifNull("this head doesn't exist: $it in $s")
                    },
                    ImageLocation(yamlObject) + loc,
                    yamlObject.getAsBoolean("outline", false),
                    yamlObject.getAsInt("layer", 0),
                    yamlObject.get("align")?.asString().toLayoutAlign(),
                    yamlObject.get("follow")?.asString(),
                    yamlObject.toConditions()
                )
            )
        }
    }
    val conditions = section.toConditions()

    val animation = section.get("animations")?.asObject()?.let { animations ->
        AnimationLocation(animations)
    } ?: AnimationLocation.zero
}