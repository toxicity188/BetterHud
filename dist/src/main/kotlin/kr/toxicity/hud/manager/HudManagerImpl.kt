package kr.toxicity.hud.manager

import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.HudManager
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.util.*

object HudManagerImpl : BetterHudManager, HudManager {

    private val hudMap = HashMap<String, HudImpl>()

    override fun start() {

    }

    override fun getHud(name: String): Hud? = hudMap[name]

    override fun reload(info: ReloadInfo, resource: GlobalResource) {
        hudMap.clear()
        DATA_FOLDER.subFolder("huds").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runWithExceptionHandling(info.sender, "Unable to load this hud: $s in ${file.name}") {
                hudMap.putSync("hud", s) {
                    HudImpl(file.path, s, resource, yamlObject)
                }
            }
        }
    }

    override fun postReload() {
        hudMap.values.forEach {
            it.jsonArray = null
        }
    }

    override fun getAllNames(): MutableSet<String> = Collections.unmodifiableSet(hudMap.keys)

    override fun getDefaultHuds(): Set<Hud> = hudMap.values.filter {
        it.isDefault
    }.toSet()

    override fun getAllHuds(): Set<Hud> {
        return hudMap.values.toSet()
    }

    override fun end() {
    }
}