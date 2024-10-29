package kr.toxicity.hud.manager

import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.renderer.BackgroundRenderer
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.util.concurrent.ConcurrentHashMap

object BackgroundManager : BetterHudManager {

    private val backgroundMap = HashMap<String, HudBackground>()
    private val backgroundRendererMap = ConcurrentHashMap<ShaderGroup, BackgroundRenderer>()

    override fun start() {

    }

    fun getBackground(name: String) = backgroundMap[name]

    override fun reload(sender: Audience, resource: GlobalResource) {
        val folder = DATA_FOLDER.subFolder("backgrounds")
        backgroundMap.clear()
        backgroundRendererMap.clear()
        folder.forEachAllYaml(sender) { file, id, yamlObject ->
            runWithExceptionHandling(sender, "Unable to load this background: $id in ${file.name}") {
                backgroundMap.putSync("background", id) {
                    HudBackground(
                        file.path,
                        id,
                        yamlObject
                    )
                }
            }
        }
    }

    @Synchronized
    fun getRenderer(shaderGroup: ShaderGroup) = backgroundRendererMap[shaderGroup]
    @Synchronized
    fun setRenderer(shaderGroup: ShaderGroup, backgroundRenderer: BackgroundRenderer) {
        backgroundRendererMap[shaderGroup] = backgroundRenderer
    }

    override fun end() {
    }
}