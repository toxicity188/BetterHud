package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.location.animation.AnimationType
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.manager.ShaderManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*

class HudImpl(
    override val path: String,
    private val internalName: String,
    resource: GlobalResource,
    section: YamlObject
): Hud, HudConfiguration {
    companion object {
        const val DEFAULT_BIT = 13
        const val MAX_BIT = 23 - DEFAULT_BIT
        const val ADD_HEIGHT = (1 shl DEFAULT_BIT - 1) - 1

        fun createBit(shader: HudShader, y: Int, consumer: (Int) -> Unit) {
            ShaderManagerImpl.addHudShader(shader) { id ->
                consumer(-((id + (1 shl MAX_BIT) shl DEFAULT_BIT) + ADD_HEIGHT + y))
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

    private val elements = section.get("layouts")?.asObject().ifNull("layout configuration not set.").mapSubConfiguration { s, yamlObject ->
        val layout = yamlObject.get("name")?.asString().ifNull("name value not set: $s").let {
            LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
        }
        var gui = GuiLocation(yamlObject)
        yamlObject.get("gui")?.asObject()?.let {
            gui += GuiLocation(it)
        }
        val pixel = yamlObject.get("pixel")?.asObject()?.let {
            PixelLocation(it)
        }  ?: PixelLocation.zero
        HudAnimation(
            layout.animation.type,
            layout.animation.location.map {
                HudElement(
                    this@HudImpl,
                    resource,
                    layout,
                    gui,
                    it + pixel
                )
            }
        )
    }.ifEmpty {
        throw RuntimeException("layout is empty.")
    }
    init {
        jsonArray?.let { array ->
            if (spaces.isNotEmpty() && !BOOTSTRAP.useLegacyFont()) array.add(jsonObjectOf(
                "type" to "space",
                "advances" to jsonObjectOf(*spaces.map {
                    it.value to it.key
                }.toTypedArray())
            ))
            PackGenerator.addTask(resource.font + "$imageEncoded.json") {
                jsonObjectOf("providers" to array).toByteArray()
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
                AnimationType.LOOP -> (hudPlayer.tick % elements.size).toInt()
                AnimationType.PLAY_ONCE -> hudPlayer.tick.toInt().coerceAtMost(elements.lastIndex)
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
        val animationType: AnimationType,
        val elements: List<HudElement>
    )
}