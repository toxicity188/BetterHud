package kr.toxicity.hud.player

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.manager.HudManager
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.ShaderManager
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.floor

class HudPlayerImpl(private val player: Player): HudPlayer {
    companion object {
        private const val MAX_WIDTH = 4096
    }
    private var tick = 0L
    private var last: WidthComponent = EMPTY_WIDTH_COMPONENT
    private var additionalComp: WidthComponent? = null
    private val variable = HashMap<String, String>()
    private val task = asyncTaskTimer(1, 1) {
        PlaceholderManagerImpl.update(this)
        tick++
        val compList = ArrayList<WidthComponent>()
        ConfigManager.defaultHud.forEach {
            HudManager.getHud(it)?.let { hud ->
                compList.addAll(hud.getComponent(this))
            }
        }
        if (compList.isNotEmpty()) {
            additionalComp?.let {
                compList.add(it)
            }
            val max = MAX_WIDTH
            val maxComp = (-max).toSpaceComponent()
            var comp = EMPTY_WIDTH_COMPONENT + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
            compList.forEachIndexed { index, it ->
                val minus = (max - it.width).toDouble() / 2
                val fMinus = floor(minus).toInt()
                val cMinus = ceil(minus).toInt()
                comp += fMinus.toSpaceComponent() + it + cMinus.toSpaceComponent()
                if (index < compList.lastIndex) comp += maxComp
            }
            last = comp

            PLUGIN.nms.showBossBar(player, color ?: ShaderManager.barColor, comp.component)
        } else PLUGIN.nms.showBossBar(player, color ?: ShaderManager.barColor, EMPTY_COMPONENT)
    }
    private var color: BarColor? = null

    override fun getHudComponent(): WidthComponent = last
    override fun getAdditionalComponent(): WidthComponent? = additionalComp
    override fun setAdditionalComponent(component: WidthComponent?) {
        additionalComp = component
    }

    override fun getBarColor(): BarColor? = color
    override fun setBarColor(color: BarColor?) {
        this.color = color
    }

    override fun getTick(): Long = tick
    override fun getBukkitPlayer(): Player = player
    override fun getVariableMap(): MutableMap<String, String> = variable
    override fun cancel() {
        PLUGIN.nms.removeBossBar(player)
        task.cancel()
    }
}