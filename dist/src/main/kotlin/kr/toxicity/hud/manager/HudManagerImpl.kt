package kr.toxicity.hud.manager

import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.manager.HudManager
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.util.*

object HudManagerImpl: BetterHudManager, HudManager {

    private val hudMap = HashMap<String, HudImpl>()

    override fun start() {

    }

    override fun getHud(name: String): Hud? = hudMap[name]

    override fun reload(sender: Audience, resource: GlobalResource) {
        hudMap.clear()
        DATA_FOLDER.subFolder("huds").forEachAllYamlAsync { file, s, configurationSection ->
            runWithExceptionHandling(sender, "Unable to load this hud: $s in ${file.name}") {
                hudMap.putSync("hud", s) {
                    HudImpl(file.path, s, resource.font, configurationSection)
                }
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