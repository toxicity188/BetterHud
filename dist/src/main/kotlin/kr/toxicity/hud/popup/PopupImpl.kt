package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.manager.LayoutManager
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManager
import kr.toxicity.hud.manager.TriggerManagerImpl
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.*
import org.bukkit.configuration.ConfigurationSection
import java.io.File
import java.util.UUID

class PopupImpl(
    file: File,
    val name: String,
    section: ConfigurationSection
): Popup {
    companion object {
        private val keyMap = HashMap<UUID, PopupUpdater>()
    }
    val gui = GuiLocation(section)
    val move = section.getConfigurationSection("move")?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getInt("duration", -1)
    private val update = section.getBoolean("update", true)
    private val group = section.getString("group") ?: name
    private val unique = section.getBoolean("unique")
    private val dispose = section.getBoolean("dispose")
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

    private val layouts = section.getConfigurationSection("layouts")?.let {
        val target = file.subFolder(name)
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
                keyMap[event.uuid]?.update()
            }
            PlayerManager.getHudPlayer(uuid)?.let { player ->
                show(event, player, event.uuid)?.let {
                    if (keyMapping) {
                        keyMap[event.uuid] = it
                    }
                }
            }
            true
        }
        section.getStringList("triggers").forEach {
            TriggerManagerImpl.addTask(it, task)
        }
    }

    override fun show(reason: UpdateEvent, player: HudPlayer): PopupUpdater? = show(reason, player, UUID.randomUUID())
    private fun show(reason: UpdateEvent, player: HudPlayer, uuid: UUID): PopupUpdater? {
        val playerMap = player.popupGroupIteratorMap
        val get = playerMap.getOrPut(group) {
            PopupIteratorGroupImpl(dispose)
        }
        if (unique && get.contains(name)) return null
        if (get.index >= move.locations.size) return null
        val buildCondition = conditions.build(reason)
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
        get.addIterator(PopupIteratorImpl(
            uuid,
            name,
            mapper,
            valueGetter,
            cond
        ) {
            ifRemove = false
            keyMap.remove(uuid)
        })
        return object : PopupUpdater {
            override fun update() {
                updater()
            }
            override fun remove() {
                ifRemove = false
            }
        }
    }
}