package kr.toxicity.hud.pack

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PackReportTest {
    @Test
    fun `counts font json separately from other files`() {
        val report = PackReport.of(mapOf(
            "assets/x/font/a.json" to ByteArray(100),
            "assets/x/font/b.json" to ByteArray(200),
            "assets/x/textures/font/c.png" to ByteArray(50),
            "pack.mcmeta" to ByteArray(10)
        ))
        assertEquals(4, report.files)
        assertEquals(2, report.fontJsons)
        assertEquals(300L, report.fontJsonBytes)
        assertEquals(360L, report.totalBytes)
    }

    @Test
    fun `font json detection needs a font directory segment`() {
        assertTrue(PackReport.isFontJson("assets/hud/font/popup_x.json"))
        assertFalse(PackReport.isFontJson("assets/hud/lang/font.json"))
        assertFalse(PackReport.isFontJson("assets/hud/font/glyph.png"))
    }

    @Test
    fun `message is human readable`() {
        assertEquals("Resource pack: 2 files, 1.00MB total, 1 font json (0.50MB)", PackReport.of(mapOf(
            "assets/x/font/a.json" to ByteArray(524288),
            "assets/x/y.png" to ByteArray(524288)
        )).message())
    }
}
