package kr.toxicity.hud.animation

import kr.toxicity.hud.equation.TEquation
import kotlin.test.Test
import kotlin.test.assertEquals

class EquationAnimationTest {
    @Test
    fun `evaluates each channel at t`() {
        val anim = EquationAnimation(TEquation("t*2"), TEquation("-t"), TEquation("t/10"), TEquation("1+t/10"))
        assertEquals(AnimationFrame(10, -5, 0.5, 1.5), anim.frame(5))
    }

    @Test
    fun `missing channels fall back to identity`() {
        val anim = EquationAnimation(TEquation.zero, TEquation.zero, TEquation.one, TEquation.one)
        assertEquals(AnimationFrame.identity, anim.frame(7))
    }
}
