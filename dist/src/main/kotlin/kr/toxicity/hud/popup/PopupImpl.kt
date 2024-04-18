package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.EquationPairLocation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.*
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import kotlin.collections.ArrayList

class PopupImpl(
    file: List<String>,
    val internalName: String,
    section: ConfigurationSection
): Popup {
    val gui = GuiLocation(section)
    val move = section.getConfigurationSection("move")?.let {
        EquationPairLocation(it)
    } ?: EquationPairLocation.zero
    private val duration = section.getInt("duration", -1)
    private val update = section.getBoolean("update", true)
    private val group = section.getString("group") ?: internalName
    private val unique = section.getBoolean("unique")
    private val dispose = section.getBoolean("dispose")
    private val queue = duration > 0 && section.getBoolean("queue")
    private val alwaysCheckCondition = queue && section.getBoolean("always-check-condition", true)
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
        val target = ArrayList(file).apply {
            add(internalName)
        }
        ArrayList<PopupLayout>().apply {
            it.forEachSubConfiguration { s, configurationSection ->
                val layout = configurationSection.getString("name").ifNull("name value not set.")
                var loc = GuiLocation(configurationSection)
                configurationSection.getConfigurationSection("gui")?.let {
                    loc += GuiLocation(it)
                }
                add(PopupLayout(
                    LayoutManager.getLayout(layout).ifNull("this layout doesn't exist: $layout"),
                    this@PopupImpl,
                    s,
                    loc,
                    configurationSection.getConfigurationSection("pixel")?.let {
                        ImageLocation(it)
                    } ?: ImageLocation.zero,
                    ArrayList(target).apply {
                        add(s)
                    },
                ))
            }
        }
    }.ifNull("layouts configuration not set.").ifEmpty {
        throw RuntimeException("layouts is empty.")
    }

    private val conditions = section.toConditions()

    init {
        val task = task@ { event: UpdateEvent, uuid: UUID ->
            PlayerManager.getHudPlayer(uuid)?.let { player ->
                if (keyMapping) {
                    player.popupKeyMap[event.key]?.let {
                        if (it.update()) return@task true
                        else player.popupKeyMap.remove(event.key)
                    }
                }
                show(event, player, event.key)?.let {
                    if (keyMapping) {
                        player.popupKeyMap[event.key] = it
                    }
                }
            }
            true
        }
        section.getConfigurationSection("triggers")?.forEachSubConfiguration { _, configurationSection ->
            TriggerManagerImpl.addTask(configurationSection, task)
        }
    }

    override fun getMaxStack(): Int = move.locations.size
    override fun show(reason: UpdateEvent, player: HudPlayer): PopupUpdater? = show(reason, player, UUID.randomUUID())
    private fun show(reason: UpdateEvent, player: HudPlayer, key: Any): PopupUpdater? {
        val get = player.popupGroupIteratorMap.computeIfAbsent(group) {
            PopupIteratorGroupImpl(dispose)
        }
        val buildCondition = conditions.build(reason)
        if (!buildCondition(player)) return null
        var updater = {
        }
        val valueMap = layouts.map {
            it.getComponent(reason)
        }
        val mapper: (Int, Int) -> List<WidthComponent> = if (update) {
            { t, index ->
                valueMap.map {
                    it(player, t, index)
                }
            }
        } else {
            fun getValue() = valueMap.map { func ->
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
                (++i < duration) && old()
            }
        }
        val value = index?.invoke(reason)?.invoke(player) ?: -1
        val remove0 = {
            player.popupKeyMap.remove(key)
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

        return internalName == other.internalName
    }

    override fun getName(): String = internalName
    override fun getGroupName(): String = group
    override fun hashCode(): Int = internalName.hashCode()
    override fun isDefault(): Boolean = default
}