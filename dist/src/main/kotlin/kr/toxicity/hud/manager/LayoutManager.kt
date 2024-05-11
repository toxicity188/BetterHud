package kr.toxicity.hud.manager

import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

object LayoutManager: BetterHudManager {

    private val layoutMap = HashMap<String, LayoutGroup>()

    override fun start() {

    }

    fun getLayout(name: String) = synchronized(layoutMap) {
        layoutMap[name]
    }

    override fun reload(resource: GlobalResource) {
        synchronized(layoutMap) {
            layoutMap.clear()
        }
        DATA_FOLDER.subFolder("layouts").forEachAllYamlAsync { file, s, configurationSection ->
            runWithExceptionHandling("Unable to load this layout: $s in ${file.name}") {
                layoutMap.putSync("layout", s) {
                    LayoutGroup(file.path, configurationSection)
                }
            }
        }
    }

    override fun end() {
    }
}