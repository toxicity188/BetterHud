package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
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
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.util.*

class PopupImpl(
    override val path: String,
    resource: GlobalResource,
    val internalName: String,
    section: YamlObject
) : Popup, HudConfiguration {
    val gui = GuiLocation(section)
    val move = section.get("move")?.asObject()?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getAsInt("duration", -1)
    private val update = section.getAsBoolean("update", true)
    private val group = section.get("group")?.asString() ?: internalName
    private val unique = section.getAsBoolean("unique", false)
    private val queue = duration > 0 && section.getAsBoolean("queue", false)
    private val push = !queue && section.getAsBoolean("push", false)
    private val alwaysCheckCondition = queue && section.getAsBoolean("always-check-condition", true)
    private val default = ConfigManagerImpl.defaultPopup.contains(internalName) || section.getAsBoolean("default", false)
    private val keyMapping = section.getAsBoolean("key-mapping", false)
    private val index: ((UpdateEvent) -> (HudPlayer) -> Int)? = section.get("index")?.asString()?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (clazz != java.lang.Number::class.java) throw RuntimeException("this index is not a number. it is ${clazz.simpleName}.")
        }.let {
            { reason ->
                it.build(reason).let { placeholder ->
                    { hudPlayer ->
                        (placeholder(hudPlayer) as Number).toInt()
                    }
                }
            }
        }
    }

    private val imageEncoded = "popup_${name}_image".encodeKey()
    var array: JsonArray? = JsonArray()
    val imageKey = createAdventureKey(imageEncoded)

    private val spaces = HashMap<Int, String>()
    private var imageChar = 0xCE000

    fun getOrCreateSpace(int: Int) = spaces.computeIfAbsent(int) {
        newChar()
    }
    fun newChar(): String = (++imageChar).parseChar()

    private val sortType = section.get("sort")?.asString()?.let {
        PopupSortType.valueOf(it.uppercase())
    } ?: PopupSortType.LAST

    private val layouts = section.get("layouts")?.asObject()?.let {
        val json = array.ifNull("error is occurred.")
        it.mapSubConfiguration { _, yamlObject ->
            val layout = yamlObject.get("name")?.asString().ifNull("name value not set.")
            var loc = GuiLocation(yamlObject)
            yamlObject.get("gui")?.asObject()?.let { gui ->
                loc += GuiLocation(gui)
            }
            PopupLayout(
                json,
                LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                this@PopupImpl,
                loc,
                yamlObject.get("pixel")?.asObject()?.let { pixel ->
                    PixelLocation(pixel)
                } ?: PixelLocation.zero,
                resource.font,
            )
        }
    }.ifNull("layouts configuration not set.").ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.toConditions()

    init {
        val task = task@ { event: UpdateEvent, uuid: UUID ->
            PlayerManagerImpl.getHudPlayer(uuid)?.let { hudPlayer ->
                show(event, hudPlayer)
            }
        }
        section.get("triggers")?.asObject()?.forEachSubConfiguration { _, yamlObject ->
            TriggerManagerImpl.getTrigger(yamlObject).registerEvent { t, u ->
                task(u, t)
            }
        }
        section.get("hide-triggers")?.asObject()?.forEachSubConfiguration { _, yamlObject ->
            TriggerManagerImpl.getTrigger(yamlObject).registerEvent { t, u ->
                PlayerManagerImpl.getHudPlayer(t)?.let {
                    hide(it)
                } ?: false
                task(u, t)
            }
        }
        array?.let { arr ->
            if (spaces.isNotEmpty() && !BOOTSTRAP.useLegacyFont()) arr.add(jsonObjectOf(
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
    private fun show0(reason: UpdateEvent, hudPlayer: HudPlayer): PopupUpdater? {
        val key = reason.key
        val get = hudPlayer.popupGroupIteratorMap.computeIfAbsent(group) {
            PopupIteratorGroupImpl()
        }
        val buildCondition = conditions.build(reason)
        if (!buildCondition(hudPlayer)) return null
        var updater = {
        }
        val valueMap = layouts.map {
            it.getComponent(reason)
        }
        val mapper: (Int, Int) -> List<WidthComponent> = if (update) {
            { index, t ->
                valueMap.map {
                    it(hudPlayer, index, t)
                }
            }
        } else {
            fun getValue() = valueMap.map { func ->
                { a: Int, b: Int ->
                    func(hudPlayer, a, b)
                }
            }
            var allValues = getValue()
            updater = {
                allValues = getValue()
            }
            val mapper2: (Int, Int) -> List<WidthComponent> = { index, t ->
                allValues.map {
                    it(index, t)
                }
            }
            mapper2
        }
        var cond = {
            buildCondition(hudPlayer)
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
        val value = index?.invoke(reason)?.invoke(hudPlayer) ?: -1
        val remove0 = {
            hudPlayer.popupKeyMap.remove(key)
            Unit
        }
        val iterator = PopupIteratorImpl(
            this,
            unique,
            lastIndex,
            key,
            sortType,
            internalName,
            queue,
            push,
            alwaysCheckCondition,
            mapper,
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
                if (get.index == 0) hudPlayer.popupGroupIteratorMap.remove(group)
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

        return internalName == other.internalName
    }

    override fun getName(): String = internalName
    override fun getGroupName(): String = group
    override fun hashCode(): Int = internalName.hashCode()
    override fun isDefault(): Boolean = default
}