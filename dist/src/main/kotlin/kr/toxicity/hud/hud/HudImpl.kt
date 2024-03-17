package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.manager.ShaderManager
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import java.io.File

class HudImpl(private val internalName: String, file: File, section: ConfigurationSection) : Hud {
    companion object {
        const val DEFAULT_BIT = 13
        const val MAX_BIT = 23 - DEFAULT_BIT
        const val ADD_HEIGHT = (1 shl (DEFAULT_BIT - 1)) - 1

        fun createBit(y: Int, shader: HudShader): Int {
            return -(((ShaderManager.addHudShader(shader) + (1 shl MAX_BIT)) shl DEFAULT_BIT) + ADD_HEIGHT + y)
        }
    }

    var imageChar = 0xCE000
    val imageKey = Key.key("$NAME_SPACE:hud/$internalName/image")
    val jsonArray = JsonArray()
    private val default = ConfigManager.defaultHud.contains(internalName) || section.getBoolean("default")
    var textIndex = 0

    private val elements = run {
        val subFile = file.subFolder(internalName)
        ArrayList<List<HudElement>>().apply {
            section.getConfigurationSection("layouts").ifNull("layout configuration not set.").forEachSubConfiguration { s, configurationSection ->
                val layout = configurationSection.getString("name").ifNull("name value not set: $s").let {
                    LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
                }
                val gui = GuiLocation(configurationSection)
                add(layout.animation.map {
                    HudElement(
                        this@HudImpl,
                        internalName,
                        subFile,
                        layout,
                        gui,
                        ImageLocation(it.x, it.y)
                    )
                })
            }
        }.ifEmpty {
            throw RuntimeException("layout is empty.")
        }
    }
    init {
        JsonObject().apply {
            add("providers", jsonArray)
        }.save(file.subFolder(internalName).subFile("image.json"))
    }


    private val conditions = section.toConditions().build(UpdateEvent.EMPTY)

    override fun getComponents(player: HudPlayer): List<WidthComponent> {
        if (!conditions(player)) return emptyList()
        return elements.map {
            it[(player.tick % it.size).toInt()].getComponent(player)
        }
    }

    override fun getName(): String = internalName
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HudImpl

        return internalName == other.internalName
    }

    override fun hashCode(): Int {
        return internalName.hashCode()
    }

    override fun isDefault(): Boolean = default
}