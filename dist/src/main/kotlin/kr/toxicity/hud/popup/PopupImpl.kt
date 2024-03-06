package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.placeholder.Conditions
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.sum
import org.bukkit.configuration.ConfigurationSection
import java.io.File

class PopupImpl(
    file: File,
    val name: String,
    section: ConfigurationSection
): Popup {
    val gui = GuiLocation(section)
    val move = section.getConfigurationSection("move")?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getInt("duration", -1)
    private val update = section.getBoolean("update", true)
    private val group = section.getString("group") ?: name
    private val unique = section.getBoolean("unique", true)

    private val layouts = section.getConfigurationSection("layouts")?.let {
        val target = file.subFolder(name)
        ArrayList<PopupLayout>().apply {
            it.forEachSubConfiguration { s, configurationSection ->
                val layout = configurationSection.getString("name").ifNull("name value not set.")
                add(PopupLayout(
                    LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                    this@PopupImpl,
                    s,
                    GuiLocation(configurationSection),
                    target.subFolder(s),
                ))
            }
        }
    }.ifNull("layouts configuration not set.").ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.getConfigurationSection("conditions")?.let {
        Conditions.parse(it)
    } ?: { true }

    override fun show(player: HudPlayer) {
        val playerMap = player.popupGroupIteratorMap
        val get = playerMap.getOrPut(group) {
            PopupIteratorGroupImpl()
        }
        if (unique && get.contains(name)) return
        if (get.index >= move.locations.size) return
        val mapper: (Int, Int) -> List<WidthComponent> = if (update) {
            { t, index ->
                layouts.map {
                    it.getComponent(t, index, player)
                }
            }
        } else {
            val allValues = layouts.map {
                it.getComponent(player)
            }
            val mapper2: (Int, Int) -> List<WidthComponent> = { t, index ->
                allValues.map {
                    it(t, index)
                }
            }
            mapper2
        }
        var cond = {
            conditions(player)
        }
        if (duration > 0) {
            val old = cond
            var i = 0
            cond = {
                (++i < duration) && old()
            }
        }
        get.addIterator(PopupIteratorImpl(
            name,
            mapper,
            duration,
            cond
        ))
    }
}