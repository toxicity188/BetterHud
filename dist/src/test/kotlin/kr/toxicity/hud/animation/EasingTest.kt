package kr.toxicity.hud.animation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EasingTest {
    @Test
    fun `every easing starts at zero and ends at one`() {
        Easing.entries.forEach {
            assertEquals(0.0, it.apply(0.0), 1e-9, "$it at 0")
            assertEquals(1.0, it.apply(1.0), 1e-9, "$it at 1")
        }
    }

    @Test
    fun `out back overshoots past the target`() {
        assertTrue(Easing.OUT_BACK.apply(0.7) > 1.0)
    }

    @Test
    fun `step holds until the end`() {
        assertEquals(0.0, Easing.STEP.apply(0.99))
        assertEquals(1.0, Easing.STEP.apply(1.0))
    }

    @Test
    fun `interpolate clamps k and maps the range`() {
        assertEquals(10.0, Easing.LINEAR.interpolate(10.0, 20.0, -1.0))
        assertEquals(15.0, Easing.LINEAR.interpolate(10.0, 20.0, 0.5))
        assertEquals(20.0, Easing.LINEAR.interpolate(10.0, 20.0, 2.0))
    }

    @Test
    fun `parse accepts dashes and any case, rejects unknown`() {
        assertEquals(Easing.OUT_CUBIC, Easing.parse("out-cubic"))
        assertEquals(Easing.IN_OUT_QUAD, Easing.parse("In_Out_Quad"))
        assertEquals(Easing.LINEAR, Easing.parse(null))
        assertFailsWith<RuntimeException> { Easing.parse("wobble") }
    }
}
