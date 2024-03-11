package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.*
import org.bukkit.configuration.ConfigurationSection
import java.io.File
import java.util.UUID

class PopupImpl(
    file: File,
    val internalName: String,
    section: ConfigurationSection
): Popup {
    companion object {
        private val keyMap = HashMap<Any, PopupUpdater>()
    }
    val gui = GuiLocation(section)
    val move = section.getConfigurationSection("move")?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getInt("duration", -1)
    private val update = section.getBoolean("update", true)
    private val group = section.getString("group") ?: internalName
    private val unique = section.getBoolean("unique")
    private val dispose = section.getBoolean("dispose")
    private val default = ConfigManager.defaultPopup.contains(internalName) || section.getBoolean("default")
    private val keyMapping = section.getBoolean("key-mapping")
    private val index: ((UpdateEvent) -> (HudPlayer) -> Int)? = section.getString("index")?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (clazz != java.lang.Number::class.java) throw RuntimeException("this index is not a number. it is ${clazz.simpleName}.")
        }.let {
            { reason ->
                it.build(reason).let { placeholder ->
                    { player ->
                        (placeholder(player) as Number).toInt()
                    }
                }
            }
        }
    }
    private val sortType = section.getString("sort")?.let {
        PopupSortType.valueOf(it.uppercase())
    } ?: PopupSortType.LAST

    private val layouts = section.getConfigurationSection("layouts")?.let {
        val target = file.subFolder(internalName)
        ArrayList<PopupLayout>().apply {
            it.forEachSubConfiguration { s, configurationSection ->
                val layout = configurationSection.getString("name").ifNull("name value not set.")
                add(PopupLayout(
                    LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                    this@PopupImpl,
                    s,
                    GuiLocation(configurationSection),
                    target.subFolder(s),
                ))
            }
        }
    }.ifNull("layouts configuration not set.").ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.toConditions()

    init {
        val task = task@ { event: UpdateEvent, uuid: UUID ->
            if (keyMapping) {
                keyMap[event.key]?.let {
                    if (it.update()) return@task true
                    else keyMap.remove(event.key)
                }
            }
            PlayerManager.getHudPlayer(uuid)?.let { player ->
                show(event, player, event.key)?.let {
                    if (keyMapping) {
                        keyMap[event.key] = it
                    }
                }
            }
            true
        }
        section.getConfigurationSection("triggers")?.forEachSubConfiguration { _, configurationSection ->
            TriggerManagerImpl.addTask(configurationSection, task)
        }
    }

    override fun show(reason: UpdateEvent, player: HudPlayer): PopupUpdater? = show(reason, player, UUID.randomUUID())
    private fun show(reason: UpdateEvent, player: HudPlayer, key: Any): PopupUpdater? {
        val playerMap = player.popupGroupIteratorMap
        val get = playerMap.getOrPut(group) {
            PopupIteratorGroupImpl(dispose)
        }
        if (unique && get.contains(internalName)) return null
        if (get.index >= move.locations.size) return null
        val buildCondition = conditions.build(reason)
        if (!buildCondition(player)) return null
        var updater = {
        }
        val mapper: (Int, Int) -> List<WidthComponent> = if (update) {
            layouts.map {
                it.getComponent(reason)
            }.let { map ->
                { t, index ->
                    map.map {
                        it(player, t, index)
                    }
                }
            }
        } else {
            fun getValue() = layouts.map {
                it.getComponent(reason)
            }.map { func ->
                { a: Int, b: Int ->
                    func(player, a, b)
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
        var ifRemove = true
        var cond = {
            ifRemove && buildCondition(player)
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
        var valueGetter: () -> Int = {
            -1
        }
        index?.let {
            val parse = it(reason)
            valueGetter = {
                parse(player)
            }
        }
        val remove0 = {
            ifRemove = false
            keyMap.remove(key)
            Unit
        }
        get.addIterator(PopupIteratorImpl(
            move.locations.lastIndex,
            key,
            sortType,
            internalName,
            mapper,
            valueGetter,
            cond,
            remove0
        ))
        return object : PopupUpdater {
            override fun update(): Boolean {
                if (!ifRemove) return false
                updater()
                return true
            }
            override fun remove() {
                remove0()
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

    override fun hashCode(): Int = internalName.hashCode()
    override fun isDefault(): Boolean = default
}