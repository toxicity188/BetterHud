package kr.toxicity.hud.manager

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.image.enums.ImageType
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ImageManager : BetterHudManager {

    override val managerName: String = "Image"
    override val supportExternalPacks: Boolean = true

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

    override fun preReload() {
        imageMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        val assets = workingDirectory.subFolder("assets")
        val map = HashMap<String, ImageElement>()
        workingDirectory.subFolder("images").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runCatching {
                val image = ImageType.valueOf(
                    yamlObject["type"]?.asString().ifNull { "type value not set." }.uppercase()
                ).createElement(assets, info.sender, file, s, yamlObject)
                map.putSync("image") {
                    image
                }
            }.handleFailure(info) {
                "Unable to load this image: $s in ${file.name}"
            }
        }
        map.values.forEach { value ->
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
        imageMap += map
    }

    override fun postReload() {
        imageNameComponent.clear()
    }

    override fun end() {
    }
}