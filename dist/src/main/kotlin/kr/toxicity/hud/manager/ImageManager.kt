package kr.toxicity.hud.manager

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.image.enums.ImageType
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.util.concurrent.ConcurrentHashMap

object ImageManager : BetterHudManager {

    private val imageMap = HashMap<String, ImageElement>()
    private val imageNameComponent = ConcurrentHashMap<HudLayout.Identifier, WidthComponent>()

    val allImage get() = imageMap.values

    @Synchronized
    fun getImage(group: HudLayout.Identifier) = imageNameComponent[group]
    @Synchronized
    fun setImage(group: HudLayout.Identifier, component: WidthComponent) {
        imageNameComponent[group] = component
    }

    override fun start() {
    }

    fun getImage(name: String) = synchronized(imageMap) {
        imageMap[name]
    }


    override fun reload(info: ReloadInfo, resource: GlobalResource) {
        synchronized(imageMap) {
            imageMap.clear()
            imageNameComponent.clear()
        }
        val assets = DATA_FOLDER.subFolder("assets")
        DATA_FOLDER.subFolder("images").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runWithExceptionHandling(info.sender, "Unable to load this image: $s in ${file.name}") {
                val image = ImageType.valueOf(
                    yamlObject["type"]?.asString().ifNull("type value not set.").uppercase()
                ).createElement(assets, info.sender, file, s, yamlObject)
                imageMap.putSync("image") {
                    image
                }
            }
        }
        imageMap.values.forEach { value ->
            val list = value.image
            if (list.isNotEmpty()) {
                list.distinctBy {
                    it.name
                }.forEach {
                    PackGenerator.addTask(resource.textures + it.name) {
                        it.image.image.toByteArray()
                    }
                }
            }
        }
    }

    override fun postReload() {
        imageNameComponent.clear()
    }

    override fun end() {
    }
}