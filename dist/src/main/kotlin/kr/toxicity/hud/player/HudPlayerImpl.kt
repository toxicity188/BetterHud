package kr.toxicity.hud.player

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.hud.Hud
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class HudPlayerImpl(private val player: Player): HudPlayer {

    private val popups = ConfigManager.defaultPopup.mapNotNull {
        PopupManagerImpl.getPopup(it)
    }.toMutableSet()
    private val huds = ConfigManager.defaultHud.mapNotNull {
        HudManagerImpl.getHud(it)
    }.toMutableSet()

    private var tick = 0L
    private var last: WidthComponent = EMPTY_WIDTH_COMPONENT
    private var additionalComp: WidthComponent? = null
    private val variable = HashMap<String, String>()
    private val popupGroup = ConcurrentHashMap<String, PopupIteratorGroup>()
    private val task = asyncTaskTimer(1, 1) {
        PlaceholderManagerImpl.update(this)
        tick++
        val compList = ArrayList<WidthComponent>()

        if (!PLUGIN.isOnReload) {
            popups.removeIf {
                runCatching {
                    it.show(UpdateEvent.EMPTY, this)
                    false
                }.onFailure { e ->
                    warn("Unable to update popup. reason: ${e.message}")
                }.getOrDefault(true)
            }
            huds.removeIf {
                runCatching {
                    compList.addAll(it.getComponents(this))
                    false
                }.onFailure { e ->
                    warn("Unable to update hud. reason: ${e.message}")
                }.getOrDefault(true)
            }
            val popupGroupIterator = popupGroup.values.iterator()
            while (popupGroupIterator.hasNext()) {
                val comp = popupGroupIterator.next().next()
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
            var comp = EMPTY_WIDTH_COMPONENT + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
            compList.forEach {
                comp += it + (-it.width).toSpaceComponent()
            }
            last = comp

            PLUGIN.nms.showBossBar(player, color ?: ShaderManager.barColor, comp.component)
        } else PLUGIN.nms.showBossBar(player, color ?: ShaderManager.barColor, EMPTY_COMPONENT)
    }
    private var color: BarColor? = null
    private val autoSave = asyncTaskTimer(6000, 6000) {
        save()
    }

    override fun getHudComponent(): WidthComponent = last
    override fun getAdditionalComponent(): WidthComponent? = additionalComp
    override fun setAdditionalComponent(component: WidthComponent?) {
        additionalComp = component
    }

    override fun getBarColor(): BarColor? = color
    override fun setBarColor(color: BarColor?) {
        this.color = color
    }


    override fun getHuds(): MutableSet<Hud> = huds
    override fun getPopups(): MutableSet<Popup> = popups

    override fun getPopupGroupIteratorMap(): MutableMap<String, PopupIteratorGroup> = popupGroup

    override fun getTick(): Long = tick
    override fun getBukkitPlayer(): Player = player
    override fun getVariableMap(): MutableMap<String, String> = variable
    private fun save() {
        DatabaseManagerImpl.currentDatabase.save(this)
    }
    override fun cancel() {
        popupGroup.forEach {
            it.value.clear()
        }
        PLUGIN.nms.removeBossBar(player)
        task.cancel()
        autoSave.cancel()
        save()
    }
}