package kr.toxicity.hud.shader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShaderBudgetTest {
    @Test
    fun `budget is ten bits`() {
        assertEquals(1023, ShaderBudget.MAX_SHADER_ID)
    }

    @Test
    fun `reports usage and flags overflow`() {
        assertFalse(ShaderBudget(1023).exceeded)
        assertTrue(ShaderBudget(1024).exceeded)
        assertEquals("Shader ids used: 12 / 1023", ShaderBudget(12).message())
        assertTrue(ShaderBudget(2000).message().startsWith("Shader ids exceeded: 2000 / 1023"))
    }
}
