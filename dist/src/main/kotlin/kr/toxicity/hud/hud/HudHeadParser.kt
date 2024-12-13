package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType.*
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.manager.EncodeManager
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*

class HudHeadParser(parent: HudImpl, private val head: HeadLayout, gui: GuiLocation, pixel: PixelLocation) {

    private val renderer = run {
        val final = head.location + pixel
        val render = head.renderScale + pixel
        val shader = HudShader(
            gui,
            render,
            head.layer,
            head.outline,
            final.opacity,
            head.property
        )
        val hair = when (head.type) {
            STANDARD -> shader
            FANCY -> HudShader(
                gui,
                render * 1.125,
                head.layer + 1,
                true,
                final.opacity,
                head.property
            )
        }
        HeadRenderer(
            head,
            parent.getOrCreateSpace(-1),
            parent.getOrCreateSpace(-(head.source.pixel * 8 + 1)),
            parent.getOrCreateSpace(-(head.source.pixel + 1)),
            (0..7).map { i ->
                val encode = "pixel_${head.source.pixel}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)
                val fileName = "$NAME_SPACE_ENCODED:$encode.png"
                val ascent = final.y + i * head.source.pixel
                val height = head.source.pixel
                val char = parent.newChar
                val mainChar = head(head.identifier(shader, ascent, fileName)) {
                    parent.jsonArray?.let { array ->
                        createAscent(shader, ascent) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            )
                        }
                    }
                    char
                }
                when (head.type) {
                    STANDARD -> HeadKey(mainChar, mainChar)
                    FANCY -> {
                        HeadKey(
                            mainChar,
                            head(head.identifier(hair, ascent - head.source.pixel, fileName)) {
                                val twoChar = parent.newChar
                                parent.jsonArray?.let { array ->
                                    createAscent(hair, ascent - head.source.pixel) { y ->
                                        array += jsonObjectOf(
                                            "type" to "bitmap",
                                            "file" to fileName,
                                            "ascent" to y,
                                            "height" to height,
                                            "chars" to jsonArrayOf(twoChar)
                                        )
                                    }
                                }
                                twoChar
                            }
                        )
                    }
                }
            },
            parent.imageKey,
            head.source.pixel * 8,
            final.x
        ).getHead(UpdateEvent.EMPTY)
    }

    fun getHead(hudPlayer: HudPlayer) = renderer(hudPlayer)
}