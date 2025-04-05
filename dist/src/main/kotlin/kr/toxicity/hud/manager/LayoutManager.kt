package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File

object LayoutManager : BetterHudManager {

    override val managerName: String = "Layout"
    override val supportExternalPacks: Boolean = true

    private val layoutMap = HashMap<String, LayoutGroup>()

    override fun start() {

    }

    fun getLayout(name: String) = synchronized(layoutMap) {
        layoutMap[name]
    }

    override fun preReload() {
        layoutMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        workingDirectory.subFolder("layouts").forEachAllYaml(info.sender) { file, s, yamlObject ->
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