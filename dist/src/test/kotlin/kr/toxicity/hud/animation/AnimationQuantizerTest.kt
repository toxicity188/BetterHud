package kr.toxicity.hud.animation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnimationQuantizerTest {
    @Test
    fun `opacity snaps to the step and stays inside zero to one`() {
        val q = AnimationQuantizer(opacityStep = 0.125)
        assertEquals(0.5, q.opacity(0.47))
        assertEquals(0.0, q.opacity(-3.0))
        assertEquals(1.0, q.opacity(1.7))
    }

    @Test
    fun `scale snaps to the step and never goes negative`() {
        val q = AnimationQuantizer(scaleStep = 0.05)
        assertEquals(1.05, q.scale(1.06))
        assertEquals(2.2, q.scale(2.2))
        assertEquals(0.0, q.scale(-1.0))
    }

    @Test
    fun `zero step disables quantization but keeps clamping`() {
        val q = AnimationQuantizer.none
        assertEquals(0.4321, q.opacity(0.4321))
        assertEquals(1.0, q.opacity(3.0))
    }

    @Test
    fun `quantize only touches opacity and scale`() {
        val q = AnimationQuantizer(0.5, 0.5)
        assertEquals(AnimationFrame(3, -7, 0.5, 1.5), q.quantize(AnimationFrame(3, -7, 0.6, 1.6)))
    }

    @Test
    fun `hidden frames collapse to a single hidden state`() {
        val q = AnimationQuantizer()
        assertEquals(AnimationFrame.hidden, q.quantize(AnimationFrame(9, 9, 0.3, 2.0, visible = false)))
    }

    @Test
    fun `rejects negative steps`() {
        assertFailsWith<IllegalArgumentException> { AnimationQuantizer(-0.1, 0.05) }
    }
}
