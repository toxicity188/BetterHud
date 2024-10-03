package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
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

class HudImpl(
    override val path: String,
    private val internalName: String,
    file: List<String>,
    section: YamlObject
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
    private val spaces = HashMap<Int, String>()
    private val default = ConfigManagerImpl.defaultHud.contains(internalName) || section.getAsBoolean("default", false)
    var textIndex = 0

    fun getOrCreateSpace(int: Int) = spaces.computeIfAbsent(int) {
        (++imageChar).parseChar()
    }

    private val elements = ArrayList<HudAnimation>().apply {
        section.get("layouts")?.asObject().ifNull("layout configuration not set.").forEachSubConfiguration { s, yamlObject ->
            val layout = yamlObject.get("name")?.asString().ifNull("name value not set: $s").let {
                LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
            }
            var gui = GuiLocation(yamlObject)
            yamlObject.get("gui")?.asObject()?.let {
                gui += GuiLocation(it)
            }
            val pixel = yamlObject.get("pixel")?.asObject()?.let {
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
            if (spaces.isNotEmpty()) array.add(JsonObject().apply {
                addProperty("type", "space")
                add("advances", JsonObject().apply {
                    spaces.forEach {
                        addProperty(it.value, it.key)
                    }
                })
            })
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

    override fun getComponents(hudPlayer: HudPlayer): List<WidthComponent> {
        if (!conditions(hudPlayer)) return emptyList()
        return elements.map {
            val elements = it.elements
            elements[when (it.animationType) {
                LayoutAnimationType.LOOP -> (hudPlayer.tick % elements.size).toInt()
                LayoutAnimationType.PLAY_ONCE -> hudPlayer.tick.toInt().coerceAtMost(elements.lastIndex)
            }].getComponent(hudPlayer)
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