package kr.toxicity.hud.player

import kr.toxicity.command.SenderType
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudComponentSupplier
import kr.toxicity.hud.api.configuration.HudObject
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupUpdater
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.util.*
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.forEach

abstract class HudPlayerImpl : HudPlayer {
    private val locationSet = HashSet<PointedLocation>()
    private val componentMap = ConcurrentHashMap<HudObject.Identifier, HudComponentSupplier<*>>()

    private var tick = 0L
    private var last: WidthComponent = EMPTY_WIDTH_COMPONENT
    private var additionalComp: WidthComponent? = null
    private val variable = ConcurrentHashMap<String, String>()
    private val popupGroup = ConcurrentHashMap<String, PopupIteratorGroup>()
    private val popupKey = ConcurrentHashMap<Any, PopupUpdater>()
    private var color: BossBar.Color? = null
    private var enabled = true
    private val pointers: MutableSet<PointedLocation> = OverridableSet(keyMapper = {
        it.name
    })
    private val componentCache = ConcurrentHashMap<Component, Long>()

    private val task = HudPlayerTask {
        val speed = ConfigManagerImpl.tickSpeed
        if (speed > 0) asyncTaskTimer(1, speed) {
            update()
        } else null
    }
    private val autoSave = HudPlayerTask {
        asyncTaskTimer(ConfigManagerImpl.autoSaveTime, ConfigManagerImpl.autoSaveTime) {
            save()
        }
    }
    private val locationProvide = HudPlayerTask {
        asyncTaskTimer(ConfigManagerImpl.locationProvideTime, ConfigManagerImpl.locationProvideTime) {
            PlayerManagerImpl.provideLocation(this)
        }
    }

    protected fun inject() {
        HudObjectType.types().forEach { type ->
            type.defaultObjects().forEach { it.add(this) }
        }
        VOLATILE_CODE.inject(this, ShaderManagerImpl.barColor)
    }

    final override fun getHudComponent(): WidthComponent = last
    final override fun getAdditionalComponent(): WidthComponent? = additionalComp
    final override fun setAdditionalComponent(component: WidthComponent?) {
        additionalComp = component
    }

    final override fun getHudObjects(): MutableMap<HudObject.Identifier, HudComponentSupplier<*>> = componentMap

    final override fun getBarColor(): BossBar.Color? = color
    final override fun setBarColor(color: BossBar.Color?) {
        this.color = color
    }

    final override fun getPointedLocation(): MutableSet<PointedLocation> = locationSet

    final override fun cancelTick() {
        task.cancel()
    }

    final override fun startTick() {
        task.restart()
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

    @Synchronized
    final override fun update() {
        updatePlaceholder()
        tick++
        val compList = ArrayList<WidthComponent>()

        if (enabled && !PLUGIN.isOnReload) {
            componentMap.entries.removeIf { (k, v) ->
                runCatching {
                    compList.addAll(v.get())
                    false
                }.handleFailure {
                    "Unable to update ${k}."
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
        }
        val component = if (compList.isNotEmpty() || additionalComp != null) {
            additionalComp?.let {
                compList += (-it.width / 2).toSpaceComponent() + it
            }
            var comp = NEGATIVE_ONE_SPACE_COMPONENT
            compList.forEach {
                comp += it
                comp += (-it.width).toSpaceComponent()
            }
            last = comp.finalizeFont()

            comp.component.build()
        } else EMPTY_COMPONENT

        val last = componentCache.getOrDefault(component, -1L)
        val now = System.currentTimeMillis()
        if (last != -1L && now - last < 1000) {
            return
        }

        componentCache[component] = System.currentTimeMillis()
        val compact = component.compact()
        VOLATILE_CODE.showBossBar(this, color ?: ShaderManagerImpl.barColor, compact)
    }

    override fun reload() {
        autoSave.restart()
        locationProvide.restart()
        startTick()
        val popupNames = popups.toNonDefaultNames()
        val hudNames = huds.toNonDefaultNames()
        val compassNames = compasses.toNonDefaultNames()
        popupKey.clear()
        componentMap.clear()
        popupGroup.clear()
        componentCache.clear()

        HudObjectType.types().forEach { type ->
            type.defaultObjects().forEach { it.add(this) }
        }
        hudNames.toNonDefaultHud().forEach { it.add(this) }
        popupNames.toNonDefaultPopup().forEach { it.add(this) }
        compassNames.toNonDefaultCompass().forEach { it.add(this) }
    }

    final override fun cancel() {
        popupGroup.forEach {
            it.value.clear()
        }
        VOLATILE_CODE.removeBossBar(this)
        cancelTick()
        autoSave.cancel()
        locationProvide.cancel()
        componentCache.clear()
    }

    override fun type(): SenderType = SenderType.PLAYER

    override fun pointers(): MutableSet<PointedLocation> = pointers
}