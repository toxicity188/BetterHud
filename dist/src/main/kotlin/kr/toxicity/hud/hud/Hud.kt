package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.placeholder.Conditions
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.subFolder
import net.kyori.adventure.text.format.TextColor
import org.bukkit.configuration.ConfigurationSection
import java.io.File
import kotlin.math.round

class Hud(name: String, file: File, section: ConfigurationSection) {
    companion object {
        private const val XY_ADD = 1
        const val DEFAULT_BIT = 11
        const val AND_BIT = (1 shl DEFAULT_BIT) - 1
        const val ADD_HEIGHT = 1023
    }
    private val elements = run {
        val subFile = file.subFolder(name)
        ArrayList<HudElement>().apply {
            section.getConfigurationSection("layouts").ifNull("layout configuration not set.").forEachSubConfiguration { s, configurationSection ->
                add(HudElement(
                    name,
                    subFile,
                    configurationSection.getString("name").ifNull("name value not set: $s").let {
                        LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
                    },
                    round(configurationSection.getDouble("x").coerceAtLeast(0.0).coerceAtMost(100.0) / 100 * 62).toInt() + XY_ADD,
                    round(configurationSection.getDouble("y").coerceAtLeast(0.0).coerceAtMost(100.0) / 100 * 62).toInt() + XY_ADD
                ))
            }
        }.ifEmpty {
            throw RuntimeException("layout is empty.")
        }
    }
    private val conditions = section.getConfigurationSection("conditions")?.let {
        Conditions.parse(it)
    } ?: { true }

    fun getComponent(player: HudPlayer): List<WidthComponent> {
        if (!conditions(player)) return emptyList()
        return elements.map {
            it.getComponent(player).build()
        }
    }
}