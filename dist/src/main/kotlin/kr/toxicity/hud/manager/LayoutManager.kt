package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

object LayoutManager : BetterHudManager {

    private val layoutMap = HashMap<String, LayoutGroup>()

    override fun start() {

    }

    fun getLayout(name: String) = synchronized(layoutMap) {
        layoutMap[name]
    }

    override fun reload(info: ReloadInfo, resource: GlobalResource) {
        synchronized(layoutMap) {
            layoutMap.clear()
        }
        DATA_FOLDER.subFolder("layouts").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runCatching {
                layoutMap.putSync("layout") {
                    LayoutGroup(s, info.sender, yamlObject)
                }
            }.onFailure {
                it.handle(info.sender, "Unable to load this layout: $s in ${file.name}")
            }
        }
    }

    override fun end() {
    }
}