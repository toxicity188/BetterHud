package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PopupManager
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.popup.PopupImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.util.*

object PopupManagerImpl: BetterHudManager, PopupManager {
    private val popupMap = HashMap<String, PopupImpl>()
    override fun start() {

    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        popupMap.clear()
        DATA_FOLDER.subFolder("popups").forEachAllYamlAsync { file, s, configurationSection ->
            runCatching {
                popupMap.putSync("popup", s) {
                    PopupImpl(file.path, resource.font, s, configurationSection)
                }
            }.onFailure { e ->
                warn(
                    "Unable to load this popup: $s in ${file.name}",
                    "Reason: ${e.message}"
                )
            }
        }
    }

    override fun getAllNames(): MutableSet<String> = Collections.unmodifiableSet(popupMap.keys)
    override fun getPopup(name: String): Popup? = popupMap[name]
    override fun getDefaultPopups(): Set<Popup> = popupMap.values.filter {
        it.isDefault
    }.toSet()
    override fun end() {
    }
}