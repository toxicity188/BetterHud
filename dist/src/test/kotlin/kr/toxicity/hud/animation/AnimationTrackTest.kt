package kr.toxicity.hud.animation

import kr.toxicity.hud.yaml.YamlObjectImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationTrackTest {
    private fun yaml(vararg pairs: Pair<String, Any>) = YamlObjectImpl("", linkedMapOf(*pairs))

    @Test
    fun `none is a single identity state`() {
        assertTrue(AnimationTrack.none.isStatic)
        assertEquals(1, AnimationTrack.none.duration)
    }

    @Test
    fun `identical frames collapse into one baked state`() {
        val track = AnimationTrack(AnimationType.PLAY_ONCE, listOf(
            AnimationFrame(0, 0, 0.0, 1.0),
            AnimationFrame(0, 0, 0.0, 1.0),
            AnimationFrame(0, 0, 1.0, 1.0),
            AnimationFrame(0, 0, 1.0, 1.0)
        ))
        assertEquals(2, track.states.size)
        assertFalse(track.isStatic)
        val baked = track.bake { it.opacity.toString() }
        assertEquals(listOf("0.0", "1.0"), baked.states)
        assertEquals("0.0", baked.at(1))
        assertEquals("1.0", baked.at(2))
    }

    @Test
    fun `play once clamps past the end and loop wraps`() {
        val frames = (0..<4).map { AnimationFrame(it, 0, 1.0, 1.0) }
        val once = AnimationTrack(AnimationType.PLAY_ONCE, frames).bake { it.x }
        val loop = AnimationTrack(AnimationType.LOOP, frames).bake { it.x }
        assertEquals(3, once.at(99))
        assertEquals(1, loop.at(5))
        assertEquals(3, AnimationTrack(AnimationType.PLAY_ONCE, frames).frameAt(50).x)
    }

    @Test
    fun `equation source is quantized per frame`() {
        val track = AnimationTrack.of(AnimationType.PLAY_ONCE, 5, EquationAnimation(
            kr.toxicity.hud.equation.TEquation.zero,
            kr.toxicity.hud.equation.TEquation.zero,
            kr.toxicity.hud.equation.TEquation("t/4"),
            kr.toxicity.hud.equation.TEquation.one
        ), AnimationQuantizer(opacityStep = 0.5))
        assertEquals(listOf(0.0, 0.5, 0.5, 1.0, 1.0), track.frames.map { it.opacity })
        assertEquals(3, track.states.size)
    }

    @Test
    fun `parses the legacy equation form`() {
        val track = AnimationTrack.parse(yaml(
            "type" to "play_once",
            "duration" to 3,
            "x-equation" to "t",
            "y-equation" to "0",
            "opacity-equation" to "1"
        ))
        assertEquals(AnimationType.PLAY_ONCE, track.type)
        assertEquals(listOf(0, 1, 2), track.frames.map { it.x })
        assertEquals(3, track.duration)
    }

    @Test
    fun `parses the keyframe form and infers duration`() {
        val track = AnimationTrack.parse(yaml(
            "type" to "play_once",
            "scale-step" to 0.1,
            "keyframes" to listOf(
                linkedMapOf("t" to 0, "scale" to 2.0, "opacity" to 0.0),
                linkedMapOf("t" to 4, "scale" to 1.0, "opacity" to 1.0, "easing" to "linear")
            )
        ))
        assertEquals(5, track.duration)
        assertEquals(listOf(2.0, 1.8, 1.5, 1.3, 1.0), track.frames.map { it.scale })
        assertEquals(1.0, track.frames.last().opacity)
    }

    @Test
    fun `hidden keyframes bake nothing until the element appears`() {
        val track = AnimationTrack.parse(yaml(
            "type" to "play_once",
            "keyframes" to listOf(
                linkedMapOf("t" to 0, "visible" to false),
                linkedMapOf("t" to 4, "visible" to true, "x" to -30),
                linkedMapOf("t" to 8, "x" to 0)
            )
        ))
        assertEquals(9, track.duration)
        assertTrue(track.frames.take(4).none { it.visible })
        assertEquals(1 + 5, track.states.size)
        val baked = track.bake { if (it.visible) it.x else null }
        assertEquals(null, baked.at(3))
        assertEquals(-30, baked.at(4))
        assertEquals(0, baked.at(100))
    }

    @Test
    fun `explicit duration wins over keyframe inference`() {
        val track = AnimationTrack.parse(yaml(
            "duration" to 10,
            "keyframes" to listOf(linkedMapOf("t" to 0, "x" to 1))
        ))
        assertEquals(10, track.duration)
        assertEquals(1, track.states.size)
    }
}
