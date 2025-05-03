package kr.toxicity.hud.manager

import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.HudManager
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File
import java.util.*

object HudManagerImpl : BetterHudManager, HudManager {

    override val managerName: String = "HUD"
    override val supportExternalPacks: Boolean = true

    private val hudMap = HashMap<String, HudImpl>()

    override fun start() {

    }

    override fun getHud(name: String): Hud? = hudMap[name]

    override fun preReload() {
        hudMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        workingDirectory.subFolder("huds").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runCatching {
                hudMap.putSync("hud") {
                    HudImpl(s, resource, yamlObject)
                }
            }.handleFailure(info) {
                "Unable to load this hud: $s in ${file.name}"
            }
        }
    }

    override fun postReload() {
        hudMap.values.forEach {
            it.jsonArray = null
        }
    }

    override fun getAllNames(): MutableSet<String> = Collections.unmodifiableSet(hudMap.keys)

    override fun getAllHuds(): Set<Hud> {
        return hudMap.values.toSet()
    }

    override fun end() {
    }
}