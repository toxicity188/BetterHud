package kr.toxicity.hud.background

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.image.NamedLoadedImage
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.renderer.BackgroundRenderer
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import java.io.File
import kotlin.math.roundToInt

//TODO Fix this
class HudBackground(
    override val path: String,
    private val id: String,
    private val yamlObject: YamlObject
) : HudConfiguration {

    private val lineMap = HashMap<Int, List<ImageTriple>>()

    private val location = PixelLocation(yamlObject)
    private val imageBuilder = BackgroundType.valueOf(yamlObject.getAsString("type", "auto").uppercase())

    fun generateImage(resource: GlobalResource, line: Int, index: Int): ImageTriple {
        val triple = lineMap.computeIfAbsent(line) {
            val image = imageBuilder.parse(id, line, yamlObject)
            val leftX = image.maxOf {
                it.first.xOffset
            }
            val nameMap = run {
                val list = ArrayList<ImageTriple>()
                var i = 0
                image.forEach {
                    list.add(ImageTriple(
                        leftX,
                        it.first.toNamed("background_${id}_${line}_${++i}"),
                        it.second.toNamed("background_${id}_${line}_${++i}"),
                        it.third.toNamed("background_${id}_${line}_${++i}")
                    ))
                }
                list
            }
            nameMap.forEach {
                it.forEach { image ->
                    PackGenerator.addTask(resource.textures + image.name) {
                        image.image.image.toByteArray()
                    }
                }
            }
            nameMap
        }
        return if (index == 0) triple.first() else if (index == triple.lastIndex) triple.last() else triple[1.coerceAtMost(triple.lastIndex)]
    }

    inner class ImageTriple(
        private val max: Int,
        private val first: NamedLoadedImage,
        private val second: NamedLoadedImage,
        private val third: NamedLoadedImage
    ) : Iterable<NamedLoadedImage> {
        override fun iterator(): Iterator<NamedLoadedImage> = listOf(first, second, third).iterator()
//        fun toBackground(shaders: HudShader, y: Int, scale: Double): BackgroundRenderer {
//            var char = start
//            fun NamedLoadedImage.toWidth(): WidthComponent {
//                val c = (++char).parseChar()
//                val height = (image.image.height.toDouble() * scale).roundToInt()
//                val div = height.toDouble() / image.image.height.toDouble()
//                HudImpl.createBit(shaders, y + location.y) {
//                    json.add(jsonObjectOf(
//                        "type" to "bitmap",
//                        "file" to "$NAME_SPACE_ENCODED:$name",
//                        "ascent" to it,
//                        "height" to height,
//                        "chars" to jsonArrayOf(c)
//                    ))
//                }
//                return WidthComponent(Component.text().content(c), (image.image.width * div).roundToInt())
//            }
//            return BackgroundRenderer(
//                location.x,
//                BackgroundRenderer.BackgroundComponent(
//                    max,
//                    first.toWidth(),
//                    second.toWidth(),
//                    third.toWidth()
//                )
//            )
//        }
    }

    enum class BackgroundType {
        AUTO {
            override fun parse(id: String, line: Int, yamlObject: YamlObject): List<BackgroundImage> {
                val image = File(DATA_FOLDER.subFolder("assets"), yamlObject.get("file")
                    .ifNull("value 'file' not set in $id")
                    .asString()
                    .replace('/', File.separatorChar))
                    .ifNotExist {
                        "This file doesn't exist: $id in $name"
                    }
                    .toImage()
                return BackgroundImage.splitOf(line, image)
            }
        }
        ;
        abstract fun parse(id: String, line: Int, yamlObject: YamlObject): List<BackgroundImage>
    }
}