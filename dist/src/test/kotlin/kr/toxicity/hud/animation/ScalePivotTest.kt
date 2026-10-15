package kr.toxicity.hud.animation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScalePivotTest {
    @Test
    fun `center pivot moves the top left by half the size difference`() {
        assertEquals(ScalePivot.Offset(-20, -10), ScalePivot.CENTER.offset(40.0, 20.0, 2.0))
        assertEquals(ScalePivot.Offset(10, 5), ScalePivot.CENTER.offset(40.0, 20.0, 0.5))
    }

    @Test
    fun `top left pivot never moves`() {
        assertEquals(ScalePivot.Offset(0, 0), ScalePivot.TOP_LEFT.offset(40.0, 20.0, 3.0))
    }

    @Test
    fun `bottom pivot keeps the baseline fixed`() {
        assertEquals(ScalePivot.Offset(-20, -20), ScalePivot.BOTTOM.offset(40.0, 20.0, 2.0))
    }

    @Test
    fun `identity scale has no offset`() {
        ScalePivot.entries.forEach {
            assertEquals(ScalePivot.Offset(0, 0), it.offset(33.0, 17.0, 1.0), "$it")
        }
    }

    @Test
    fun `parse accepts dashes`() {
        assertEquals(ScalePivot.BOTTOM_LEFT, ScalePivot.parse("bottom-left"))
        assertEquals(ScalePivot.CENTER, ScalePivot.parse(null))
    }
}
