package kr.toxicity.hud.player

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

internal class HudRenderCache {
    private var component: Component? = null
    private var color: BossBar.Color? = null

    @Synchronized
    fun shouldUpdate(component: Component, color: BossBar.Color, force: Boolean): Boolean {
        if (!force && component == this.component && color == this.color) {
            return false
        }
        this.component = component
        this.color = color
        return true
    }

    @Synchronized
    fun invalidate() {
        component = null
        color = null
    }
}
