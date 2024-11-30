package kr.toxicity.hud.player

import kr.toxicity.command.SenderType
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
import net.kyori.adventure.bossbar.BossBar
import java.util.concurrent.ConcurrentHashMap

abstract class HudPlayerImpl : HudPlayer {
    private val locationSet = HashSet<PointedLocation>()
    private val objectSet = HashSet<HudObject>()

    private var tick = 0L
    private var last: WidthComponent = EMPTY_WIDTH_COMPONENT
    private var additionalComp: WidthComponent? = null
    private val variable = HashMap<String, String>()
    private val popupGroup = ConcurrentHashMap<String, PopupIteratorGroup>()
    private val popupKey = HashMap<Any, PopupUpdater>()
    private var task: HudTask? = null
    private var color: BossBar.Color? = null
    private var enabled = true
    private val pointers: MutableSet<PointedLocation> = OverridableSet(keyMapper = {
        it.name
    })
    private val autoSave = asyncTaskTimer(6000, 6000) {
        save()
    }
    private val locationProvide = asyncTaskTimer(20, 20) {
        PlayerManagerImpl.provideLocation(this)
    }
    protected fun inject() {
        objectSet += HudManagerImpl.defaultHuds
        objectSet += PopupManagerImpl.defaultPopups
        objectSet += CompassManagerImpl.defaultCompasses
        startTick()
        VOLATILE_CODE.inject(this, ShaderManagerImpl.barColor)
    }

    final override fun getHudComponent(): WidthComponent = last
    final override fun getAdditionalComponent(): WidthComponent? = additionalComp
    final override fun setAdditionalComponent(component: WidthComponent?) {
        additionalComp = component
    }
    final override fun getHudObjects(): MutableSet<HudObject> = objectSet

    final override fun getBarColor(): BossBar.Color? = color
    final override fun setBarColor(color: BossBar.Color?) {
        this.color = color
    }

    final override fun getPointedLocation(): MutableSet<PointedLocation> = locationSet

    final override fun cancelTick() {
        task?.cancel()
        task = null
    }

    final override fun startTick() {
        cancelTick()
        VOLATILE_CODE.reloadBossBar(this, ShaderManagerImpl.barColor)
        val speed = ConfigManagerImpl.tickSpeed
        if (speed > 0) task = asyncTaskTimer(1, speed) {
            update()
        }
    }

    final override fun getPopupGroupIteratorMap(): MutableMap<String, PopupIteratorGroup> = popupGroup
    final override fun getPopupKeyMap(): MutableMap<Any, PopupUpdater> = popupKey

    final override fun getTick(): Long = tick
    final override fun getVariableMap(): MutableMap<String, String> = variable
    final override fun getHead(): HudPlayerHead = PlayerHeadManager.provideHead(name())
    final override fun isHudEnabled(): Boolean = enabled
    final override fun setHudEnabled(toEnable: Boolean) {
        enabled = toEnable
    }
    final override fun save() {
        val current = DatabaseManagerImpl.currentDatabase
        if (!current.isClosed) current.save(this)
    }

    protected abstract fun updatePlaceholder()

    final override fun update() {
        updatePlaceholder()
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
                } else compList += comp
            }
        } else {
            popupGroup.clear()
        }
        if (compList.isNotEmpty() || additionalComp != null) {
            additionalComp?.let {
                compList += ((-it.width / 2).toSpaceComponent() + it)
            }
            var comp = NEGATIVE_ONE_SPACE_COMPONENT
            compList.forEach {
                comp += it + (-it.width).toSpaceComponent()
            }
            last = comp.finalizeFont()

            VOLATILE_CODE.showBossBar(this, color ?: ShaderManagerImpl.barColor, comp.component.build())
        } else VOLATILE_CODE.showBossBar(this, color ?: ShaderManagerImpl.barColor, EMPTY_COMPONENT)
    }

    final override fun resetElements() {
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
        objectSet += PopupManagerImpl.defaultPopups
        objectSet += HudManagerImpl.defaultHuds
        objectSet += CompassManagerImpl.defaultCompasses
        popupNames.forEach {
            PopupManagerImpl.getPopup(it)?.let { popup ->
                if (!popup.isDefault) objectSet += popup
            }
        }
        hudNames.forEach {
            HudManagerImpl.getHud(it)?.let { hud ->
                if (!hud.isDefault) objectSet += hud
            }
        }
        compassNames.forEach {
            CompassManagerImpl.getCompass(it)?.let { hud ->
                if (!hud.isDefault) objectSet += hud
            }
        }
    }

    final override fun cancel() {
        popupGroup.forEach {
            it.value.clear()
        }
        VOLATILE_CODE.removeBossBar(this)
        cancelTick()
        autoSave.cancel()
        locationProvide.cancel()
    }

    override fun type(): SenderType = SenderType.PLAYER

    override fun pointers(): MutableSet<PointedLocation> = pointers
}