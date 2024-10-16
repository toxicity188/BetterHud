package kr.toxicity.hud.manager

import kr.toxicity.hud.api.compass.Compass
import kr.toxicity.hud.api.manager.CompassManager
import kr.toxicity.hud.compass.CompassImpl
import kr.toxicity.hud.compass.CompassType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience

object CompassManagerImpl : BetterHudManager, CompassManager {

    private val compassMap = HashMap<String, CompassImpl>()

    override fun start() {
    }


    override fun reload(sender: Audience, resource: GlobalResource) {
        compassMap.clear()
        val assets = DATA_FOLDER.subFolder("assets")
        DATA_FOLDER.subFolder("compasses").forEachAllYaml(sender) { f, s, c ->
            runWithExceptionHandling(sender, "Unable to load this compass: $s in ${f.name}") {
                compassMap.putSync("compass", s) {
                    c.get("type")?.asString().ifNull("type value not set.").run {
                        CompassType.valueOf(uppercase()).build(resource, assets, f.path, s, c)
                    }
                }
            }
        }
    }

    override fun end() {
    }

    override fun getCompass(name: String): Compass? = synchronized(compassMap) {
        compassMap[name]
    }
    override fun getAllNames(): MutableSet<String> = compassMap.keys

    override fun getDefaultCompasses(): MutableSet<Compass> = compassMap.values.filter {
        it.isDefault
    }.toMutableSet()
}