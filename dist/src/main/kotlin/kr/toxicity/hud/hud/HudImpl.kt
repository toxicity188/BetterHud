package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import kr.toxicity.hud.api.configuration.HudComponentSupplier
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.manager.EncodeManager
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.renderer.bake.AnimatedLayout
import kr.toxicity.hud.renderer.bake.ElementHost
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*

class HudImpl(
    override val id: String,
    resource: GlobalResource,
    section: YamlObject
) : Hud, HudConfiguration, ElementHost, PlaceholderSource by PlaceholderSource.Impl(section) {

    private var imageChar = 0xCE000

    override val newChar
        get() = (++imageChar).parseChar()

    private val imageEncoded = "hud_${id}_image".encodeKey(EncodeManager.EncodeNamespace.FONT)
    override val imageKey = createAdventureKey(imageEncoded)
    private var jsonArray: JsonArray? = JsonArray()
    override val hostKind: String get() = "hud"
    override val hostName: String get() = id
    override val json: JsonArray? get() = jsonArray
    private val spaces = intKeyMapOf<String>()
    private val default = ConfigManagerImpl.defaultHud.contains(id) || section.getAsBoolean("default", false)
    private var textIndex = 0
    private val tick = section.getAsLong("tick", 1)

    override fun getOrCreateSpace(int: Int): String = spaces.computeIfAbsent(int) {
        newChar
    }

    override fun nextTextIndex(): Int = ++textIndex
    override fun releaseJson() {
        jsonArray = null
    }

    private var layoutIndex = 0
    private val elements = section["layouts"]?.asObject().ifNull { "layout configuration not set." }.mapSubConfiguration { s, yamlObject ->
        val layout = yamlObject["name"]?.asString().ifNull { "name value not set: $s" }.let {
            LayoutManager.getLayout(it).ifNull { "this layout doesn't exist: $it" }
        }
        var gui = GuiLocation(yamlObject)
        yamlObject["gui"]?.asObject()?.let {
            gui += GuiLocation(it)
        }
        val pixel = yamlObject["pixel"]?.asObject()?.let {
            PixelLocation(it)
        }  ?: PixelLocation.zero
        AnimatedLayout(
            this@HudImpl,
            resource.font,
            layout,
            gui,
            pixel,
            ++layoutIndex
        )
    }.ifEmpty {
        throw RuntimeException("layout is empty.")
    }
    init {
        jsonArray?.let { array ->
            if (spaces.isNotEmpty()) array += jsonObjectOf(
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

    override fun tick(): Long = tick

    private val conditions = section.toConditions(this) build UpdateEvent.EMPTY

    override fun createRenderer(player: HudPlayer): HudComponentSupplier<Hud> {
        val map = elements.map {
            runByTick(tick, { player.tick }, it.render(UpdateEvent.EMPTY)(player))
        }
        return HudComponentSupplier.of(this) {
            if (conditions(player)) map.map { it() } else emptyList()
        }
    }

    override fun getName(): String = id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HudImpl

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun isDefault(): Boolean = default
}