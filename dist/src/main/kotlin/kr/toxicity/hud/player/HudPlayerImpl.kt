package kr.toxicity.hud.player

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObject
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.api.scheduler.HudTask
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class HudPlayerImpl(
    private val player: Player,
): HudPlayer {
    private val locationSet = HashSet<PointedLocation>()
    private val objectSet = HashSet<HudObject>()

    private var tick = 0L
    private var last: WidthComponent = EMPTY_WIDTH_COMPONENT
    private var additionalComp: WidthComponent? = null
    private val variable = HashMap<String, String>()
    private val popupGroup = ConcurrentHashMap<String, PopupIteratorGroup>()
    private val popupKey = HashMap<Any, PopupUpdater>()
    private var task: HudTask? = null
    private var color: BarColor? = null
    private var enabled = true
    private val autoSave = asyncTaskTimer(6000, 6000) {
        save()
    }
    private val locationProvide = asyncTaskTimer(20, 20) {
        PlayerManagerImpl.provideLocation(this)
    }
    init {
        objectSet.addAll(HudManagerImpl.defaultHuds)
        objectSet.addAll(PopupManagerImpl.defaultPopups)
        objectSet.addAll(CompassManagerImpl.defaultCompasses)
        startTick()
        PLUGIN.nms.inject(player, ShaderManagerImpl.barColor)
    }

    override fun getHudComponent(): WidthComponent = last
    override fun getAdditionalComponent(): WidthComponent? = additionalComp
    override fun setAdditionalComponent(component: WidthComponent?) {
        additionalComp = component
    }
    override fun getHudObjects(): MutableSet<HudObject> = objectSet

    override fun getBarColor(): BarColor? = color
    override fun setBarColor(color: BarColor?) {
        this.color = color
    }

    override fun getPointedLocation(): MutableSet<PointedLocation> = locationSet

    override fun cancelTick() {
        task?.cancel()
        task = null
    }

    override fun startTick() {
        cancelTick()
        PLUGIN.nms.reloadBossBar(player, ShaderManagerImpl.barColor)
        val speed = ConfigManagerImpl.tickSpeed
        if (speed > 0) task = asyncTaskTimer(1, speed) {
            update()
        }
    }

    override fun getPopupGroupIteratorMap(): MutableMap<String, PopupIteratorGroup> = popupGroup
    override fun getPopupKeyMap(): MutableMap<Any, PopupUpdater> = popupKey

    override fun getTick(): Long = tick
    override fun getBukkitPlayer(): Player = player
    override fun getVariableMap(): MutableMap<String, String> = variable
    override fun getHead(): HudPlayerHead = PlayerHeadManager.provideHead(player.name)
    override fun isHudEnabled(): Boolean = enabled
    override fun setHudEnabled(toEnable: Boolean) {
        enabled = toEnable
    }
    override fun save() {
        DatabaseManagerImpl.currentDatabase.save(this)
    }

    override fun update() {
        PlaceholderManagerImpl.update(this)
        tick++
        val compList = ArrayList<WidthComponent>()

        if (enabled && !PLUGIN.isOnReload) {
            objectSet.removeIf {
                runCatching {
                    compList.addAll(it.getComponentsByType(this))
                    false
                }.onFailure { e ->
                    e.printStackTrace()
                    warn("Unable to update ${it.type.name}. reason: ${e.message}")
                }.getOrDefault(true)
            }
            val popupGroupIterator = popupGroup.values.iterator()
            while (popupGroupIterator.hasNext()) {
                val next = popupGroupIterator.next()
                if (next.index == 0) {
                    popupGroupIterator.remove()
                    continue
                }
                val comp = next.next()
                if (comp.isEmpty()) {
                    popupGroupIterator.remove()
                } else compList.addAll(comp)
            }
        } else {
            popupGroup.clear()
        }
        if (compList.isNotEmpty()) {
            additionalComp?.let {
                compList.add((-it.width / 2).toSpaceComponent() + it)
            }
            var comp = EMPTY_WIDTH_COMPONENT + NEGATIVE_ONE_SPACE_COMPONENT
            compList.forEach {
                comp += it + (-it.width).toSpaceComponent()
            }
            last = comp

            PLUGIN.nms.showBossBar(player, color ?: ShaderManagerImpl.barColor, comp.component.build())
        } else PLUGIN.nms.showBossBar(player, color ?: ShaderManagerImpl.barColor, EMPTY_COMPONENT)
    }

    override fun resetElements() {
        val popupNames = popups.filter {
            !it.isDefault
        }.map {
            it.name
        }
        val hudNames = huds.filter {
            !it.isDefault
        }.map {
            it.name
        }
        val compassNames = compasses.filter {
            !it.isDefault
        }.map {
            it.name
        }
        objectSet.clear()
        objectSet.addAll(PopupManagerImpl.defaultPopups)
        objectSet.addAll(HudManagerImpl.defaultHuds)
        objectSet.addAll(CompassManagerImpl.defaultCompasses)
        popupNames.forEach {
            PopupManagerImpl.getPopup(it)?.let { popup ->
                if (!popup.isDefault) objectSet.add(popup)
            }
        }
        hudNames.forEach {
            HudManagerImpl.getHud(it)?.let { hud ->
                if (!hud.isDefault) objectSet.add(hud)
            }
        }
        compassNames.forEach {
            CompassManagerImpl.getCompass(it)?.let { hud ->
                if (!hud.isDefault) objectSet.add(hud)
            }
        }
    }

    override fun cancel() {
        popupGroup.forEach {
            it.value.clear()
        }
        PLUGIN.nms.removeBossBar(player)
        cancelTick()
        autoSave.cancel()
        locationProvide.cancel()
    }
}