package kr.toxicity.hud.manager

import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience

//TODO Fix this
object BackgroundManager : BetterHudManager {

    private val backgroundMap = HashMap<String, HudBackground>()

    override fun start() {

    }

    fun getBackground(name: String) = backgroundMap[name]

    override fun reload(sender: Audience, resource: GlobalResource) {
        val folder = DATA_FOLDER.subFolder("backgrounds")
        backgroundMap.clear()
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

    override fun end() {
    }
}