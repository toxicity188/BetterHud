package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.manager.ShaderManager
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.subFolder
import kr.toxicity.hud.util.toConditions
import org.bukkit.configuration.ConfigurationSection
import java.io.File

class Hud(name: String, file: File, section: ConfigurationSection) {
    companion object {
        const val DEFAULT_BIT = 13
        const val MAX_BIT = 23 - DEFAULT_BIT
        const val ADD_HEIGHT = (1 shl (DEFAULT_BIT - 1)) - 1

        fun createBit(y: Int, shader: HudShader): Int {
            return -(((ShaderManager.addHudShader(shader) + (1 shl MAX_BIT)) shl DEFAULT_BIT) + ADD_HEIGHT + y)
        }
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
                    configurationSection.getDouble("x").coerceAtLeast(0.0).coerceAtMost(100.0),
                    configurationSection.getDouble("y").coerceAtLeast(0.0).coerceAtMost(100.0)
                ))
            }
        }.ifEmpty {
            throw RuntimeException("layout is empty.")
        }
    }
    private val conditions = section.toConditions().build(UpdateEvent.EMPTY)

    fun getComponent(player: HudPlayer): List<WidthComponent> {
        if (!conditions(player)) return emptyList()
        return elements.map {
            it.getComponent(player)
        }
    }
}