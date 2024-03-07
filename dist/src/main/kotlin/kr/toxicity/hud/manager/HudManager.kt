package kr.toxicity.hud.manager

import kr.toxicity.hud.hud.Hud
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.forEachAllYaml
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.warn

object HudManager: BetterHudManager {

    private val hudMap = HashMap<String, Hud>()

    override fun start() {

    }

    fun getHud(name: String) = hudMap[name]

    override fun reload(resource: GlobalResource) {
        hudMap.clear()
        val hudFolder = resource.font.subFolder("hud")
        DATA_FOLDER.subFolder("huds").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                hudMap[s] = Hud(s, hudFolder, configurationSection)
            }.onFailure { e ->
                warn("Unable to load this hud: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
    }

    override fun end() {
    }
}