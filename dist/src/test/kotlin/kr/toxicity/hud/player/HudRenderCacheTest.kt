package kr.toxicity.hud.player

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HudRenderCacheTest {
    private val cache = HudRenderCache()

    @Test
    fun `first render requires an update`() {
        assertTrue(cache.shouldUpdate(Component.empty(), BossBar.Color.WHITE, false))
    }

    @Test
    fun `structurally equal components do not require another update`() {
        assertTrue(cache.shouldUpdate(Component.text("same"), BossBar.Color.WHITE, false))
        assertFalse(cache.shouldUpdate(Component.text("same"), BossBar.Color.WHITE, false))
    }

    @Test
    fun `changed component requires an update`() {
        assertTrue(cache.shouldUpdate(Component.text("first"), BossBar.Color.WHITE, false))
        assertTrue(cache.shouldUpdate(Component.text("second"), BossBar.Color.WHITE, false))
    }

    @Test
    fun `changed color requires an update`() {
        val component = Component.text("same")
        assertTrue(cache.shouldUpdate(component, BossBar.Color.WHITE, false))
        assertTrue(cache.shouldUpdate(component, BossBar.Color.RED, false))
    }

    @Test
    fun `forced renders always require an update`() {
        val component = Component.text("same")
        assertTrue(cache.shouldUpdate(component, BossBar.Color.WHITE, false))
        assertTrue(cache.shouldUpdate(component, BossBar.Color.WHITE, true))
    }

    @Test
    fun `invalidating cache requires another update`() {
        val component = Component.text("same")
        assertTrue(cache.shouldUpdate(component, BossBar.Color.WHITE, false))
        assertFalse(cache.shouldUpdate(component, BossBar.Color.WHITE, false))

        cache.invalidate()

        assertTrue(cache.shouldUpdate(component, BossBar.Color.WHITE, false))
    }
}
