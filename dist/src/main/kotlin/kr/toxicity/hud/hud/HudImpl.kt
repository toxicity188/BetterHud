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
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

class HudImpl(
    override val path: String,
    private val internalName: String,
    resource: GlobalResource,
    section: YamlObject
) : Hud, HudConfiguration, PlaceholderSource by PlaceholderSource.Impl(section) {

    private var imageChar = 0xCE000

    val newChar
        get() = (++imageChar).parseChar()

    private val imageEncoded = "hud_${internalName}_image".encodeKey()
    val imageKey = createAdventureKey(imageEncoded)
    var jsonArray: JsonArray? = JsonArray()
    private val spaces = HashMap<Int, String>()
    private val default = ConfigManagerImpl.defaultHud.contains(internalName) || section.getAsBoolean("default", false)
    var textIndex = 0

    fun getOrCreateSpace(int: Int) = spaces.computeIfAbsent(int) {
        newChar
    }

    private val elements = section["layouts"]?.asObject().ifNull("layout configuration not set.").mapSubConfiguration { s, yamlObject ->
        val layout = yamlObject["name"]?.asString().ifNull("name value not set: $s").let {
            LayoutManager.getLayout(it).ifNull("this layout doesn't exist: $it")
        }
        var gui = GuiLocation(yamlObject)
        yamlObject["gui"]?.asObject()?.let {
            gui += GuiLocation(it)
        }
        val pixel = yamlObject["pixel"]?.asObject()?.let {
            PixelLocation(it)
        }  ?: PixelLocation.zero
        HudAnimation(
            layout.animation.type,
            layout.animation.location.map {
                HudParser(
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
            if (spaces.isNotEmpty() && !BOOTSTRAP.useLegacyFont()) array += jsonObjectOf(
                "type" to "space",
                "advances" to jsonObjectOf(*spaces.map {
                    it.value to it.key
                }.toTypedArray())
            )
            PackGenerator.addTask(resource.font + "$imageEncoded.json") {
                jsonObjectOf("providers" to array).toByteArray()
            }
        }
        jsonArray = null
    }


    override fun getType(): HudObjectType<*> {
        return HudObjectType.HUD
    }

    private val conditions = section.toConditions(this) build UpdateEvent.EMPTY

    override fun getComponents(player: HudPlayer): List<WidthComponent> {
        if (!conditions(player)) return emptyList()
        return elements.map {
            val elements = it.elements
            elements[when (it.animationType) {
                AnimationType.LOOP -> (player.tick % elements.size).toInt()
                AnimationType.PLAY_ONCE -> player.tick.toInt().coerceAtMost(elements.lastIndex)
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
        val animationType: AnimationType,
        val elements: List<HudParser>
    )
}