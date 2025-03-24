package kr.toxicity.hud.manager

import kr.toxicity.hud.api.compass.Compass
import kr.toxicity.hud.api.manager.CompassManager
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.compass.CompassImpl
import kr.toxicity.hud.compass.CompassType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File
import java.util.*

object CompassManagerImpl : BetterHudManager, CompassManager {

    override val managerName: String = "Background"
    override val supportExternalPacks: Boolean = true

    private val compassMap = HashMap<String, CompassImpl>()

    override fun start() {
    }


    override fun preReload() {
        compassMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        compassMap.clear()
        val assets = workingDirectory.subFolder("assets")
        workingDirectory.subFolder("compasses").forEachAllYaml(info.sender) { f, s, c ->
            runCatching {
                compassMap.putSync("compass") {
                    c["type"]?.asString().ifNull { "type value not set." }.run {
                        CompassType.valueOf(uppercase()).build(resource, assets, s, c)
                    }
                }
            }.onFailure {
                it.handle(info.sender, "Unable to load this compass: $s in ${f.name}")
            }
        }
    }

    override fun end() {
    }

    override fun getCompass(name: String): Compass? = synchronized(compassMap) {
        compassMap[name]
    }
    override fun getAllNames(): Set<String> = Collections.unmodifiableSet(compassMap.keys)

    override fun getDefaultCompasses(): Set<Compass> = compassMap.values.filter {
        it.isDefault
    }.toSet()

    override fun getAllCompasses(): Set<Compass> = compassMap.values.toSet()
}