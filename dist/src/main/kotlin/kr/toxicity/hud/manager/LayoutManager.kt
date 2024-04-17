package kr.toxicity.hud.manager

import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

object LayoutManager: BetterHudManager {

    private val layoutMap = HashMap<String, LayoutGroup>()

    override fun start() {

    }

    fun getLayout(name: String) = layoutMap[name]

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        layoutMap.clear()
        DATA_FOLDER.subFolder("layouts").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                layoutMap[s] = LayoutGroup(configurationSection)
            }.onFailure { e ->
                warn("Unable to load this layout: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
        callback()
    }

    override fun end() {
    }
}