package kr.toxicity.hud.manager

import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.HudManager
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.forEachAllYaml
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.warn
import java.util.Collections

object HudManagerImpl: BetterHudManager, HudManager {

    private val hudMap = HashMap<String, HudImpl>()

    override fun start() {

    }

    override fun getHud(name: String): Hud? = hudMap[name]

    override fun reload(resource: GlobalResource) {
        hudMap.clear()
        val hudFolder = resource.font.subFolder("hud")
        DATA_FOLDER.subFolder("huds").forEachAllYaml { file, s, configurationSection ->
            runCatching {
                hudMap[s] = HudImpl(s, hudFolder, configurationSection)
            }.onFailure { e ->
                warn("Unable to load this hud: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
    }

    override fun getAllNames(): MutableSet<String> = Collections.unmodifiableSet(hudMap.keys)

    override fun getDefaultHuds(): Set<Hud> = hudMap.values.filter {
        it.isDefault
    }.toSet()

    override fun end() {
    }
}