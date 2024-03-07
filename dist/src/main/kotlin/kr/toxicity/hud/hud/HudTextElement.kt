package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.Style
import java.io.File
import kotlin.math.ceil

class HudTextElement(name: String, file: File, private val text: TextLayout, index: Int, x: Double, y: Double, animation: List<ImageLocation>) {

    private val renderer = run {
        val shader = HudShader(
            GuiLocation(x, y),
            text.layer + index,
            text.outline
        )
        animation.mapIndexed { index2, imageLocation ->
            val key = Key.key("$NAME_SPACE:hud/$name/text/text_${index + 1}_${index2 + 1}")
            val array = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", "space")
                    add("advances", JsonObject().apply {
                        addProperty(" ", 4)
                    })
                })
            }
            text.text.array.forEach {
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:text/${text.text.fontName}/${it.file}")
                    addProperty("ascent", Hud.createBit((text.location.y + imageLocation.y).coerceAtLeast(-Hud.ADD_HEIGHT).coerceAtMost(Hud.ADD_HEIGHT), shader))
                    addProperty("height", ceil(text.text.height * text.scale).toInt())
                    add("chars", it.chars)
                })
            }
            JsonObject().apply {
                add("providers", array)
            }.save(File(file, "text_${index + 1}_${index2 + 1}.json"))
            TextRenderer(
                text.text.charWidth,
                Style.style(text.color).font(key),
                text.pattern,
                text.align,
                text.scale,
                text.location.x + imageLocation.x,
                text.space,
                text.numberEquation,
                text.numberFormat
            ) {
                text.conditions(it) && text.text.conditions(it)
            }
        }
    }

    fun getText(player: HudPlayer): PixelComponent = renderer[(player.tick % renderer.size).toInt()].getText(player)
}