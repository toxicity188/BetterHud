package kr.toxicity.hud.animation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KeyframeAnimationTest {
    @Test
    fun `interpolates linearly between keyframes`() {
        val anim = KeyframeAnimation(listOf(
            Keyframe(t = 0, y = -30.0, opacity = 0.0),
            Keyframe(t = 10, y = 0.0, opacity = 1.0)
        ))
        assertEquals(AnimationFrame(0, -15, 0.5, 1.0), anim.frame(5))
    }

    @Test
    fun `holds the first value before and the last value after`() {
        val anim = KeyframeAnimation(listOf(
            Keyframe(t = 4, x = 8.0),
            Keyframe(t = 8, x = 16.0)
        ))
        assertEquals(8, anim.frame(0).x)
        assertEquals(16, anim.frame(20).x)
    }

    @Test
    fun `channels are independent`() {
        val anim = KeyframeAnimation(listOf(
            Keyframe(t = 0, scale = 2.0),
            Keyframe(t = 4, opacity = 0.0),
            Keyframe(t = 8, scale = 1.0)
        ))
        assertEquals(1.5, anim.frame(4).scale)
        assertEquals(0.0, anim.frame(4).opacity)
        assertEquals(0.0, anim.frame(8).opacity)
    }

    @Test
    fun `destination keyframe easing shapes the segment`() {
        val anim = KeyframeAnimation(listOf(
            Keyframe(t = 0, x = 0.0),
            Keyframe(t = 10, x = 100.0, easing = Easing.STEP)
        ))
        assertEquals(0, anim.frame(9).x)
        assertEquals(100, anim.frame(10).x)
    }

    @Test
    fun `duration is one past the last keyframe and channels default sensibly`() {
        val anim = KeyframeAnimation(listOf(Keyframe(t = 14, x = 3.0)))
        assertEquals(15, anim.duration)
        assertEquals(AnimationFrame(3, 0, 1.0, 1.0), anim.frame(0))
    }

    @Test
    fun `unsorted keyframes are accepted`() {
        val anim = KeyframeAnimation(listOf(Keyframe(t = 10, x = 10.0), Keyframe(t = 0, x = 0.0)))
        assertEquals(5, anim.frame(5).x)
    }

    @Test
    fun `visible holds as a step until the next keyframe that sets it`() {
        val anim = KeyframeAnimation(listOf(
            Keyframe(t = 0, visible = false, x = 0.0),
            Keyframe(t = 6, visible = true, x = 12.0)
        ))
        assertEquals(false, anim.frame(5).visible)
        assertEquals(true, anim.frame(6).visible)
        assertEquals(10, anim.frame(5).x)
    }

    @Test
    fun `rejects empty list and negative time`() {
        assertFailsWith<IllegalArgumentException> { KeyframeAnimation(emptyList()) }
        assertFailsWith<IllegalArgumentException> { Keyframe(t = -1) }
    }
}
