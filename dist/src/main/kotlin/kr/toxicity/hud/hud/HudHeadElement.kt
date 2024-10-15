package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType.*
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*

class HudHeadElement(parent: HudImpl, private val head: HeadLayout, gui: GuiLocation, pixel: ImageLocation) {

    private val renderer = run {
        val final = head.location + pixel
        val shader = HudShader(
            gui,
            head.renderScale,
            head.layer,
            head.outline,
            final.opacity,
            head.property
        )
        val hair = when (head.type) {
            STANDARD -> shader
            FANCY -> HudShader(
                gui,
                head.renderScale * 1.125,
                head.layer + 1,
                true,
                final.opacity,
                head.property
            )
        }
        HeadRenderer(
            parent.getOrCreateSpace(-1),
            parent.getOrCreateSpace(-(head.head.pixel * 8 + 1)),
            parent.getOrCreateSpace(-(head.head.pixel + 1)),
            (0..7).map { i ->
                val encode = "pixel_${head.head.pixel}".encodeKey()
                val fileName = "$NAME_SPACE_ENCODED:$encode.png"
                val ascent = final.y + i * head.head.pixel
                val height = head.head.pixel
                val shaderGroup = ShaderGroup(shader, fileName, 1.0, ascent)
                val char = (++parent.imageChar).parseChar()
                val mainChar = PlayerHeadManager.getHead(shaderGroup) ?: run {
                    parent.jsonArray?.let { array ->
                        HudImpl.createBit(shader, ascent) { y ->
                            array.add(jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            ))
                        }
                    }
                    PlayerHeadManager.setHead(shaderGroup, char)
                    char
                }
                when (head.type) {
                    STANDARD -> HeadKey(mainChar, mainChar)
                    FANCY -> {
                        val hairShaderGroup = ShaderGroup(hair, fileName, 1.0, ascent - head.head.pixel)
                        HeadKey(
                            mainChar,
                            PlayerHeadManager.getHead(hairShaderGroup) ?: run {
                                val twoChar = (++parent.imageChar).parseChar()
                                parent.jsonArray?.let { array ->
                                    HudImpl.createBit(hair, ascent - head.head.pixel) { y ->
                                        array.add(jsonObjectOf(
                                            "type" to "bitmap",
                                            "file" to fileName,
                                            "ascent" to y,
                                            "height" to height,
                                            "chars" to jsonArrayOf(twoChar)
                                        ))
                                    }
                                }
                                PlayerHeadManager.setHead(hairShaderGroup, twoChar)
                                twoChar
                            }
                        )
                    }
                }
            },
            parent.imageKey,
            head.head.pixel * 8,
            final.x,
            head.align,
            head.type,
            head.follow,
            head.cancelIfFollowerNotExists,
            head.conditions.and(head.head.conditions)
        ).getHead(UpdateEvent.EMPTY)
    }

    fun getHead(hudPlayer: HudPlayer) = renderer(hudPlayer)
}