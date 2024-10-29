package kr.toxicity.hud.background

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.util.*
import java.io.File

class HudBackground(
    override val path: String,
    id: String,
    yamlObject: YamlObject
) : HudConfiguration {

    val location = PixelLocation(yamlObject)
    val image = BackgroundType.valueOf(yamlObject.getAsString("type", "auto").uppercase()).parse(id, yamlObject)

    enum class BackgroundType {
        AUTO {
            override fun parse(id: String, yamlObject: YamlObject): List<BackgroundImage> {
                val image = File(DATA_FOLDER.subFolder("assets"), yamlObject.get("file")
                    .ifNull("value 'file' not set in $id")
                    .asString()
                    .replace('/', File.separatorChar))
                    .ifNotExist {
                        "This file doesn't exist: $id in $name"
                    }
                    .toImage()
                val line = yamlObject.getAsInt("line", 1)
                return BackgroundImage.splitOf(line, image)
            }
        }
        ;
        abstract fun parse(id: String, yamlObject: YamlObject): List<BackgroundImage>
    }
}