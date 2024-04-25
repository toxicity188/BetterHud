package kr.toxicity.hud.compass

import kr.toxicity.hud.resource.GlobalResource
import org.bukkit.configuration.ConfigurationSection
import java.io.File

enum class CompassType {
    CIRCLE {
        override fun build(
            resource: GlobalResource,
            assets: File,
            path: String,
            name: String,
            section: ConfigurationSection
        ): CompassImpl {
            return CircleCompass(resource, assets, path, name, section)
        }
    }
    ;

    abstract fun build(resource: GlobalResource, assets: File, path: String, name: String, section: ConfigurationSection): CompassImpl
}