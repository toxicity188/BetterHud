package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.round

class HudImageElement(parent: HudImpl, private val image: ImageLayout, gui: GuiLocation, pixel: ImageLocation) {


    private val chars = run {
        val hud = image.image
        val isSingle = hud.image.size == 1

        val shader = HudShader(
            gui,
            image.layer,
            image.outline
        )

        val list = ArrayList<PixelComponent>()
        if (hud.listener != null) {
            list.add(EMPTY_PIXEL_COMPONENT)
        }
        val finalPixel = image.location + pixel
        hud.image.forEach { pair ->

            val c = (++parent.imageChar).parseChar()
            val height = round(pair.image.image.height.toDouble() * image.scale).toInt()
            val scale = height.toDouble() / pair.image.image.height
            val finalWidth = WidthComponent(Component.text(c).font(parent.imageKey), round((pair.image.image.width).toDouble() * scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
            parent.jsonArray.add(JsonObject().apply {
                addProperty("type", "bitmap")
                if (isSingle) addProperty("file", "$NAME_SPACE:image/${pair.name}")
                else addProperty("file", "$NAME_SPACE:image/${hud.name}/${pair.name}")
                addProperty("ascent", HudImpl.createBit((finalPixel.y).coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT), shader))
                addProperty("height", height)
                add("chars", JsonArray().apply {
                    add(c)
                })
            })
            list.add(finalWidth.toPixelComponent(finalPixel.x + round(pair.image.xOffset * scale).toInt()))
        }
        ImageRenderer(
            hud,
            image.color,
            list,
            image.conditions.and(image.image.conditions)
        )
    }

    val max = chars.max()

    fun getComponent(player: HudPlayer): PixelComponent = chars.getComponent(
        UpdateEvent.EMPTY)(player)

}