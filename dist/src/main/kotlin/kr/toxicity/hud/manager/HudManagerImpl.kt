package kr.toxicity.hud.manager

import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.HudManager
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.util.Collections

object HudManagerImpl: BetterHudManager, HudManager {

    private val hudMap = HashMap<String, HudImpl>()

    override fun start() {

    }

    override fun getHud(name: String): Hud? = hudMap[name]

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        hudMap.clear()
        val hudFolder = ArrayList(resource.font).apply {
            add("hud")
        }
        DATA_FOLDER.subFolder("huds").forEachAllYamlAsync({ file, s, configurationSection ->
            runCatching {
                hudMap.putSync("hud", s) {
                    HudImpl(file.path, s, hudFolder, configurationSection)
                }
            }.onFailure { e ->
                warn(
                    "Unable to load this hud: $s in ${file.name}",
                    "Reason: ${e.message}"
                )
            }
        }, callback)
    }

    override fun getAllNames(): MutableSet<String> = Collections.unmodifiableSet(hudMap.keys)

    override fun getDefaultHuds(): Set<Hud> = hudMap.values.filter {
        it.isDefault
    }.toSet()

    override fun end() {
    }
}