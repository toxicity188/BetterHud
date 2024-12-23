package kr.toxicity.hud.compass

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.compass.type.CircleCompass
import kr.toxicity.hud.resource.GlobalResource
import java.io.File

enum class CompassType {
    CIRCLE {
        override fun build(
            resource: GlobalResource,
            assets: File,
            name: String,
            section: YamlObject
        ): CompassImpl {
            return CircleCompass(resource, assets, name, section)
        }
    }
    ;

    abstract fun build(resource: GlobalResource, assets: File, name: String, section: YamlObject): CompassImpl
}