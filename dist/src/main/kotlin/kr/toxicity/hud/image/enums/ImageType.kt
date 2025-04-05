package kr.toxicity.hud.image.enums

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.util.*
import kr.toxicity.hud.yaml.YamlObjectImpl
import java.io.File
import java.util.regex.Pattern
import kotlin.math.roundToInt
import kotlin.text.replace

enum class ImageType {
    SINGLE {
        override fun getComponent(listener: HudListener, frame: Long, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (component.images.isNotEmpty()) {
                if (get >= 0) component.images[get
                    .coerceAtLeast(0)
                    .coerceAtMost(component.images.lastIndex)] else component.images[0]
            } else EMPTY_PIXEL_COMPONENT
        }

        override fun createElement(
            assets: File,
            sender: BetterCommandSource,
            file: File,
            s: String,
            yamlObject: YamlObject
        ): ImageElement {
            val fileName = yamlObject["file"]?.asString().ifNull { "file value not set." }
                .replace('/', File.separatorChar)
            val targetFile = File(
                assets,
                fileName
            )
            return ImageElement(
                s,
                listOf(
                    targetFile
                        .toImage()
                        .flip(yamlObject.toFlip())
                        .removeEmptySide()
                        .ifNull { "Invalid image." }
                        .toNamed(fileName.replace(File.separatorChar, '_')),
                ),
                this,
                yamlObject["setting"]?.asObject() ?: emptySetting
            )
        }

    },
    LISTENER {
        override fun getComponent(listener: HudListener, frame: Long, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (get >= 0) component.images[get
                .coerceAtLeast(0)
                .coerceAtMost(component.images.lastIndex)] else component choose frame
        }

        override fun createElement(
            assets: File,
            sender: BetterCommandSource,
            file: File,
            s: String,
            yamlObject: YamlObject
        ): ImageElement {
            val splitType = yamlObject["split-type"]?.asString()?.let { splitType ->
                runCatching {
                    SplitType.valueOf(splitType.uppercase())
                }.onFailure {
                    it.handle("Unable to find that split-type: $splitType")
                }.getOrNull()
            } ?: SplitType.LEFT
            val split = yamlObject.getAsInt("split", 25).coerceAtLeast(1)
            val fileName = yamlObject["file"]?.asString().ifNull { "file value not set." }
                .replace('/', File.separatorChar)
            val getFile = File(
                assets,
                fileName
            )
            return ImageElement(
                s,
                splitType.split(
                    getFile
                        .toImage()
                        .flip(yamlObject.toFlip())
                        .removeEmptySide()
                        .ifNull { "Invalid image." }
                        .toNamed("${fileName.replace(File.separatorChar, '_').substringBeforeLast('.')}_${splitType.name.lowercase()}_$split.png"), split
                ),
                this,
                yamlObject["setting"]?.asObject()
                    .ifNull { "setting configuration not found." }
            )
        }
    },
    SEQUENCE {
        override fun getComponent(listener: HudListener, frame: Long, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (get >= 0) component.images[get
                .coerceAtLeast(0)
                .coerceAtMost(component.images.lastIndex)] else component choose frame
        }

        override fun createElement(
            assets: File,
            sender: BetterCommandSource,
            file: File,
            s: String,
            yamlObject: YamlObject
        ): ImageElement {
            val globalFrame = yamlObject.getAsInt("frame", 1).coerceAtLeast(1)
            return ImageElement(
                s,
                (yamlObject["files"]?.asArray()?.map {
                    it.asString()
                } ?: emptyList()).ifEmpty {
                    throw RuntimeException("files are empty.")
                }.map { string ->
                    val matcher = multiFrameRegex.matcher(string)
                    var fileName = string
                    var frame = 1
                    if (matcher.find()) {
                        fileName = matcher.group("name")
                        frame = matcher.group("frame").toInt()
                    }
                    fileName = fileName.replace('/', File.separatorChar)
                    val targetFile = File(assets, fileName)
                    val targetImage = targetFile
                        .toImage()
                        .flip(yamlObject.toFlip())
                        .removeEmptyWidth()
                        .ifNull { "Invalid image: $string" }
                        .toNamed(fileName.replace(File.separatorChar, '_'))
                    (0..<(frame * globalFrame).coerceAtLeast(1)).map {
                        targetImage
                    }
                }.sum(),
                this,
                yamlObject["setting"]?.asObject() ?: emptySetting
            )
        }
    }
    ;
    companion object {
        val emptySetting = YamlObjectImpl("", mutableMapOf<String, Any>())
        private val multiFrameRegex = Pattern.compile("(?<name>(([a-zA-Z]|/|.|(_))+)):(?<frame>([0-9]+))")

        private fun YamlObject.toFlip() = get("flip")?.asArray()?.map {
            FlipType.valueOf(it.asString().uppercase())
        }?.toSet() ?: emptySet()
    }

    abstract fun getComponent(listener: HudListener, frame: Long, component: ImageComponent, player: HudPlayer): PixelComponent
    abstract fun createElement(assets: File, sender: BetterCommandSource, file: File, s: String, yamlObject: YamlObject): ImageElement
}