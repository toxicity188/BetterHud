package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.NAME_SPACE_ENCODED
import kr.toxicity.hud.util.encodeKey
import kr.toxicity.hud.util.parseChar
import net.kyori.adventure.text.Component

class HudHeadElement(parent: HudImpl, private val head: HeadLayout, gui: GuiLocation, pixel: ImageLocation) {
    private val renderer = run {
        val final = head.location + pixel
        val shader = HudShader(
            gui,
            head.layer,
            head.outline
        )
        HeadRenderer(
            (0..7).map { i ->
                val char = (++parent.imageChar).parseChar()
                parent.jsonArray.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    val encode = "pixel_${head.head.pixel}".encodeKey()
                    addProperty("file", "$NAME_SPACE_ENCODED:$encode/$encode.png")
                    addProperty("ascent", HudImpl.createBit(final.y + i * head.head.pixel, shader))
                    addProperty("height", head.head.pixel)
                    add("chars", JsonArray().apply {
                        add(char)
                    })
                })
                Component.text().content(char).font(parent.imageKey)
            },
            head.head.pixel,
            final.x,
            head.align,
            head.follow,
            head.conditions.and(head.head.conditions)
        ).getHead(UpdateEvent.EMPTY)
    }

    fun getHead(player: HudPlayer) = renderer(player)
}