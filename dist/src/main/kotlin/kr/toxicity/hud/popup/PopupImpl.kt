package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.placeholder.Conditions
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.subFolder
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
    private val update = section.getBoolean("update")
    private val group = section.getString("group") ?: name

    private val layouts = section.getConfigurationSection("layouts")?.let {
        val target = file.subFolder(name)
        ArrayList<PopupLayout>().apply {
            it.forEachSubConfiguration { s, configurationSection ->
                val layout = configurationSection.getString("name").ifNull("name value not set.")
                add(PopupLayout(
                    LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                    this@PopupImpl,
                    s,
                    target.subFolder(s),
                    configurationSection.getConfigurationSection("conditions")?.let {
                        Conditions.parse(it)
                    } ?: { true }
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
        val mapper: (Int) -> List<WidthComponent> = if (update) {
            { index ->
                layouts[get.index % layouts.size].getComponents(index, player)
            }
        } else {
            val allValues = layouts.map {
                it.getComponents(player)
            }
            val mapper2: (Int) -> List<WidthComponent> = { index ->
                allValues[get.index % layouts.size][index]
            }
            mapper2
        }
        val cond: () -> Boolean  = {
            layouts[get.index % layouts.size].condition(player)
        }
        PopupIteratorImpl(
            mapper,
            layouts.size,
            duration
        ) {
            conditions(player) && cond()
        }
    }
}