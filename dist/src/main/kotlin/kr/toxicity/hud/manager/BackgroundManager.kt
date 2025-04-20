package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File

//TODO replace it to proper background in the future.
object BackgroundManager : BetterHudManager {

    override val managerName: String = "Background"
    override val supportExternalPacks: Boolean = true

    private val backgroundMap = HashMap<String, HudBackground>()

    override fun start() {

    }

    fun getBackground(name: String) = backgroundMap[name]

    override fun preReload() {
        backgroundMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        val folder = workingDirectory.subFolder("backgrounds")
        folder.forEach {
            if (it.extension == "yml") {
                runCatching {
                    val yaml = it.toYaml()
                    val name = it.nameWithoutExtension
                    val backgroundFolder = folder.subFolder(name)
                    fun getImage(imageName: String) = File(backgroundFolder, "$imageName.png")
                        .ifNotExist { "this image doesn't exist: $imageName.png in $name" }
                        .toImage()
                        .removeEmptyWidth()
                        .ifNull { "this image is empty: $imageName.png in $name" }.apply {
                            PackGenerator.addTask(resource.textures + "${"background_${name}_$imageName".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)}.png") {
                                image.toByteArray()
                            }
                        }
                    backgroundMap.putSync("background") {
                        HudBackground(
                            name,
                            getImage("left"),
                            getImage("right"),
                            getImage("body"),
                            PixelLocation(yaml)
                        )
                    }
                }.handleFailure(info) {
                    "Unable to load this yml: ${it.name}"
                }
            }
        }
    }

    override fun end() {
    }
}