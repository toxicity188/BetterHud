package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.LayoutAnimationType
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.manager.ShaderManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import org.bukkit.configuration.ConfigurationSection

class HudImpl(
    override val path: String,
    private val internalName: String,
    file: List<String>,
    section: ConfigurationSection
): Hud, HudConfiguration {
    companion object {
        const val DEFAULT_BIT = 13
        const val MAX_BIT = 23 - DEFAULT_BIT
        const val ADD_HEIGHT = (1 shl (DEFAULT_BIT - 1)) - 1

        fun createBit(shader: HudShader, y: Int, consumer: (Int) -> Unit) {
            ShaderManagerImpl.addHudShader(shader) { id ->
                consumer(-(((id + (1 shl MAX_BIT)) shl DEFAULT_BIT) + ADD_HEIGHT + y))
            }
        }
    }

    var imageChar = 0xCE000

    private val imageEncoded = "hud_${internalName}_image".encodeKey()
    val imageKey = createAdventureKey(imageEncoded)
    var jsonArray: JsonArray? = JsonArray()
    private val default = ConfigManagerImpl.defaultHud.contains(internalName) || section.getBoolean("default")
    var textIndex = 0

    private val elements = ArrayList<HudAnimation>().apply {
        section.getConfigurationSection("layouts").ifNull("layout configuration not set.").forEachSubConfiguration { s, configurationSection ->
            val layout = configurationSection.getString("name").ifNull("name value not set: $s").let {
                LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
            }
            var gui = GuiLocation(configurationSection)
            configurationSection.getConfigurationSection("gui")?.let {
                gui += GuiLocation(it)
            }
            val pixel = configurationSection.getConfigurationSection("pixel")?.let {
                ImageLocation(it)
            } ?: ImageLocation.zero
            add(HudAnimation(
                layout.animation.type,
                layout.animation.location.map {
                    HudElement(
                        this@HudImpl,
                        file,
                        layout,
                        gui,
                        ImageLocation(it.x, it.y) + pixel
                    )
                }
            ))
        }
    }.ifEmpty {
        throw RuntimeException("layout is empty.")
    }
    init {
        jsonArray?.let { array ->
            PackGenerator.addTask(
                ArrayList(file).apply {
                    add("$imageEncoded.json")
                }
            ) {
                JsonObject().apply {
                    add("providers", array)
                }.toByteArray()
            }
        }
        jsonArray = null
    }


    override fun getType(): HudObjectType<*> {
        return HudObjectType.HUD
    }

    private val conditions = section.toConditions().build(UpdateEvent.EMPTY)

    override fun getComponents(player: HudPlayer): List<WidthComponent> {
        if (!conditions(player)) return emptyList()
        return elements.map {
            val elements = it.elements
            elements[when (it.animationType) {
                LayoutAnimationType.LOOP -> (player.tick % elements.size).toInt()
                LayoutAnimationType.PLAY_ONCE -> player.tick.toInt().coerceAtMost(elements.lastIndex)
            }].getComponent(player)
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

    private class HudAnimation(
        val animationType: LayoutAnimationType,
        val elements: List<HudElement>
    )
}