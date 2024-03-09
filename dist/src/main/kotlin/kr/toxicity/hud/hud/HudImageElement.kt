package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.ListenerHudImage
import kr.toxicity.hud.image.SplitType
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.ceil
import kotlin.math.round

class HudImageElement(parent: Hud, private val image: ImageLayout, x: Double, y: Double, animation: List<ImageLocation>) {


    private val chars = run {
        val hud = image.image
        val maxWidth = hud.image.maxOf {
            it.image.image.width
        }
        val isSingle = hud.image.size == 1

        val shader = HudShader(
            GuiLocation(x, y),
            image.layer,
            image.outline
        )


        animation.map { imageLocation ->
            val list = ArrayList<PixelComponent>()
            if (hud is ListenerHudImage) {
                list.add(EMPTY_PIXEL_COMPONENT)
            }
            hud.image.forEach { pair ->
                val c = (++parent.imageChar).parseChar()
                val height = round(pair.image.image.height.toDouble() * image.scale).toInt()
                val scale = height.toDouble() / pair.image.image.height
                val finalWidth = WidthComponent(Component.text(c).font(parent.imageKey), ceil((pair.image.image.width).toDouble() * scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                parent.jsonArray.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    if (isSingle) addProperty("file", "$NAME_SPACE:image/${pair.name}")
                    else addProperty("file", "$NAME_SPACE:image/${hud.name}/${pair.name}")
                    addProperty("ascent", Hud.createBit((image.location.y + imageLocation.y).coerceAtLeast(-Hud.ADD_HEIGHT).coerceAtMost(Hud.ADD_HEIGHT), shader))
                    addProperty("height", height)
                    add("chars", JsonArray().apply {
                        add(c)
                    })
                })
                list.add(finalWidth.toPixelComponent(image.location.x + imageLocation.x + round(pair.image.xOffset * scale).toInt()))
            }
            ImageRenderer(
                hud,
                list,
                image.conditions.and(image.image.conditions)
            )
        }

    }

    fun getComponent(player: HudPlayer): PixelComponent = chars[(player.tick % chars.size).toInt()].getComponent(
        UpdateEvent.EMPTY)(player)

}