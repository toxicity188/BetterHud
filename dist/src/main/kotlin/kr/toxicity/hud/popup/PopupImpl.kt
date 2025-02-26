package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.Popup.FrameType
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.util.*

class PopupImpl(
    override val id: String,
    resource: GlobalResource,
    section: YamlObject
) : Popup, HudConfiguration, PlaceholderSource by PlaceholderSource.Impl(section) {
    val gui = GuiLocation(section)
    val move = section["move"]?.asObject()?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getAsInt("duration", -1)
    private val group = section["group"]?.asString() ?: id
    private val unique = section.getAsBoolean("unique", false)
    private val queue = duration > 0 && section.getAsBoolean("queue", false)
    private val push = !queue && section.getAsBoolean("push", false)
    private val alwaysCheckCondition = queue && section.getAsBoolean("always-check-condition", true)
    private val default = ConfigManagerImpl.defaultPopup.contains(id) || section.getAsBoolean("default", false)
    private val keyMapping = section.getAsBoolean("key-mapping", false)
    private val index: ((UpdateEvent) -> (HudPlayer) -> Int)? = section["index"]?.asString()?.let {
        PlaceholderManagerImpl.find(it, this).assertNumber {
            "this index is not a number. it is ${clazz.simpleName}."
        }.let {
            { reason ->
                (it build reason).let { placeholder ->
                    { player ->
                        (placeholder(player) as Number).toInt()
                    }
                }
            }
        }
    }
    private val tick = section.getAsLong("tick", 1)
    private val frameType = section.getAsString("frame-type", "local").uppercase().run {
        FrameType.valueOf(this)
    }

    private val imageEncoded = "popup_${name}_image".encodeKey(EncodeManager.EncodeNamespace.FONT)
    var array: JsonArray? = JsonArray()
    val imageKey = createAdventureKey(imageEncoded)

    private val spaces = HashMap<Int, String>()
    private var imageChar = 0xCE000

    fun getOrCreateSpace(int: Int) = spaces.computeIfAbsent(int) {
        newChar
    }

    val newChar
        get() = (++imageChar).parseChar()

    private val sortType = section["sort"]?.asString()?.let {
        PopupSortType.valueOf(it.uppercase())
    } ?: PopupSortType.LAST

    private val layouts = section["layouts"]?.asObject()?.let {
        val json = array.ifNull { "error is occurred." }
        var i = 0
        it.mapSubConfiguration { _, yamlObject ->
            val layout = yamlObject["name"]?.asString().ifNull { "name value not set." }
            var loc = GuiLocation(yamlObject)
            yamlObject["gui"]?.asObject()?.let { gui ->
                loc += GuiLocation(gui)
            }
            PopupLayout(
                ++i,
                json,
                LayoutManager.getLayout(layout).ifNull { "this layout doesn't exist: $layout" },
                this@PopupImpl,
                loc,
                yamlObject["pixel"]?.asObject()?.let { pixel ->
                    PixelLocation(pixel)
                } ?: PixelLocation.zero,
                resource.font,
            )
        }
    }.ifNull { "layouts configuration not set." }.ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.toConditions(this)

    init {
        val task = { event: UpdateEvent, uuid: UUID ->
            PlayerManagerImpl.getHudPlayer(uuid)?.let { player ->
                show(event, player)
            }
        }
        section["triggers"]?.asObject()?.forEachSubConfiguration { _, yamlObject ->
            TriggerManagerImpl.getTrigger(yamlObject).registerEvent { t, u ->
                task(u, t)
            }
        }
        section["hide-triggers"]?.asObject()?.forEachSubConfiguration { _, yamlObject ->
            TriggerManagerImpl.getTrigger(yamlObject).registerEvent { t, u ->
                PlayerManagerImpl.getHudPlayer(t)?.let {
                    hide(it)
                } == true
                task(u, t)
            }
        }
        array?.let { arr ->
            if (spaces.isNotEmpty()) arr.add(jsonObjectOf(
                "type" to "space",
                "advances" to jsonObjectOf(*spaces.map {
                    it.value to it.key
                }.toTypedArray())
            ))
            PackGenerator.addTask(resource.font + "$imageEncoded.json") {
                jsonObjectOf("providers" to arr).toByteArray()
            }
        }
        array = null
    }

    override fun getType(): HudObjectType<*> = HudObjectType.POPUP
    override fun tick(): Long = tick
    override fun frameType(): FrameType = frameType

    override fun getMaxStack(): Int = move.locations.size
    override fun show(reason: UpdateEvent, player: HudPlayer): PopupUpdater? {
        if (keyMapping) {
            player.popupKeyMap[reason.key]?.let {
                if (it.update()) return null
                else player.popupKeyMap.remove(reason.key)
            }
        }
        return show0(reason, player)?.apply {
            if (keyMapping) {
                player.popupKeyMap[reason.key] = this
            }
        }
    }
    private fun show0(reason: UpdateEvent, player: HudPlayer): PopupUpdater? {
        val key = reason.key
        val get = player.popupGroupIteratorMap.computeIfAbsent(group) {
            PopupIteratorGroupImpl()
        }
        val buildCondition = conditions build reason
        if (!buildCondition(player)) return null
        var updater = {
        }
        var cond = {
            buildCondition(player)
        }
        if (duration > 0) {
            val old = cond
            var i = 0
            val old2 = updater
            updater = {
                i = 0
                old2()
            }
            cond = {
                ++i < duration && old()
            }
        }
        val value = index?.invoke(reason)?.invoke(player) ?: -1
        val remove0 = {
            player.popupKeyMap.remove(key)
            Unit
        }
        val iterator = PopupIteratorImpl(
            reason,
            player,
            layouts,
            this,
            unique,
            lastIndex,
            key,
            sortType,
            id,
            queue,
            push,
            alwaysCheckCondition,
            value,
            cond,
            remove0
        )
        get.addIterator(iterator)
        return object : PopupUpdater {
            override fun update(): Boolean {
                if (iterator.markedAsRemoval()) return false
                updater()
                return true
            }
            override fun remove() {
                iterator.remove()
                if (get.index == 0) player.popupGroupIteratorMap.remove(group)
            }
            override fun getIndex(): Int = iterator.index
            override fun setIndex(index: Int) {
                iterator.priority = index
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PopupImpl

        return id == other.id
    }

    override fun getName(): String = id
    override fun getGroupName(): String = group
    override fun hashCode(): Int = id.hashCode()
    override fun isDefault(): Boolean = default
}