package kr.toxicity.hud.manager

import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.forEachAllYaml
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.warn

object LayoutManager: MythicHudManager {

    private val layoutMap = HashMap<String, HudLayout>()

    override fun start() {

    }

    fun getLayout(name: String) = layoutMap[name]

    override fun reload(resource: GlobalResource) {
        layoutMap.clear()
        DATA_FOLDER.subFolder("layouts").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                layoutMap[s] = HudLayout(configurationSection)
            }.onFailure { e ->
                warn("Unable to load this layout: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
    }

    override fun end() {
    }
}