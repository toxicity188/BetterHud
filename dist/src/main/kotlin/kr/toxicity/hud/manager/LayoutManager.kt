package kr.toxicity.hud.manager

import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience

object LayoutManager : BetterHudManager {

    private val layoutMap = HashMap<String, LayoutGroup>()

    override fun start() {

    }

    fun getLayout(name: String) = synchronized(layoutMap) {
        layoutMap[name]
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        synchronized(layoutMap) {
            layoutMap.clear()
        }
        DATA_FOLDER.subFolder("layouts").forEachAllYaml(sender) { file, s, yamlObject ->
            runWithExceptionHandling(sender, "Unable to load this layout: $s in ${file.name}") {
                layoutMap.putSync("layout", s) {
                    LayoutGroup(file.path, sender, yamlObject)
                }
            }
        }
    }

    override fun end() {
    }
}