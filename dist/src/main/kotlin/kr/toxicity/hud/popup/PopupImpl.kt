package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.*
import kr.toxicity.hud.api.yaml.YamlObject
import java.util.*

class PopupImpl(
    override val path: String,
    file: List<String>,
    val internalName: String,
    section: YamlObject
): Popup, HudConfiguration {
    val gui = GuiLocation(section)
    val move = section.get("move")?.asObject()?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getAsInt("duration", -1)
    private val update = section.getAsBoolean("update", true)
    private val group = section.get("group")?.asString() ?: internalName
    private val unique = section.getAsBoolean("unique", false)
    private val dispose = section.getAsBoolean("dispose", false)
    private val queue = duration > 0 && section.getAsBoolean("queue", false)
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

    private val sortType = section.get("sort")?.asString()?.let {
        PopupSortType.valueOf(it.uppercase())
    } ?: PopupSortType.LAST

    private val layouts = section.get("layouts")?.asObject()?.let {
        val json = array.ifNull("error is occurred.")
        ArrayList<PopupLayout>().apply {
            it.forEachSubConfiguration { _, yamlObject ->
                val layout = yamlObject.get("name")?.asString().ifNull("name value not set.")
                var loc = GuiLocation(yamlObject)
                yamlObject.get("gui")?.asObject()?.let {
                    loc += GuiLocation(it)
                }
                add(PopupLayout(
                    json,
                    LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                    this@PopupImpl,
                    loc,
                    yamlObject.get("pixel")?.asObject()?.let {
                        ImageLocation(it)
                    } ?: ImageLocation.zero,
                    file,
                ))
            }
        }
    }.ifNull("layouts configuration not set.").ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.toConditions()

    init {
        val task = task@ { event: UpdateEvent, uuid: UUID ->
            PlayerManagerImpl.getHudPlayer(uuid)?.let { hudPlayer ->
                if (keyMapping) {
                    hudPlayer.popupKeyMap[event.key]?.let {
                        if (it.update()) return@task true
                        else hudPlayer.popupKeyMap.remove(event.key)
                    }
                }
                show(event, hudPlayer, event.key)?.let {
                    if (keyMapping) {
                        hudPlayer.popupKeyMap[event.key] = it
                    }
                }
            }
            true
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
            PackGenerator.addTask(ArrayList(file).apply {
                add("$imageEncoded.json")
            }) {
                JsonObject().apply {
                    add("providers", arr)
                }.toByteArray()
            }
        }
        array = null
    }

    override fun getType(): HudObjectType<*> = HudObjectType.POPUP

    override fun getMaxStack(): Int = move.locations.size
    override fun show(reason: UpdateEvent, hudPlayer: HudPlayer): PopupUpdater? = show(reason, hudPlayer, UUID.randomUUID())
    private fun show(reason: UpdateEvent, hudPlayer: HudPlayer, key: Any): PopupUpdater? {
        val get = hudPlayer.popupGroupIteratorMap.computeIfAbsent(group) {
            PopupIteratorGroupImpl(dispose)
        }
        val buildCondition = conditions.build(reason)
        if (!buildCondition(hudPlayer)) return null
        var updater = {
        }
        val valueMap = layouts.map {
            it.getComponent(reason)
        }
        val mapper: (Int, Int) -> List<WidthComponent> = if (update) {
            { t, index ->
                valueMap.map {
                    it(hudPlayer, t, index)
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
            val mapper2: (Int, Int) -> List<WidthComponent> = { t, index ->
                allValues.map {
                    it(t, index)
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
                (++i < duration) && old()
            }
        }
        val value = index?.invoke(reason)?.invoke(hudPlayer) ?: -1
        val remove0 = {
            hudPlayer.popupKeyMap.remove(key)
            Unit
        }
        val iterator = PopupIteratorImpl(
            unique,
            lastIndex,
            key,
            sortType,
            internalName,
            queue,
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