package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component

class HudImageElement(parent: HudImpl, private val image: ImageLayout, gui: GuiLocation, pixel: ImageLocation) {

    private val chars = run {
        val hud = image.image

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
            val fileName = "$NAME_SPACE_ENCODED:${pair.name}"
            val height = Math.round(pair.image.image.height.toDouble() * image.scale).toInt()
            val scale = height.toDouble() / pair.image.image.height
            val ascent = finalPixel.y.coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
            val shaderGroup = ShaderGroup(shader, fileName, image.scale, ascent)

            val component = ImageManager.getImage(shaderGroup) ?: run {
                val c = (++parent.imageChar).parseChar()
                val finalWidth = WidthComponent(Component.text()
                    .content(c)
                    .font(parent.imageKey)
                    .append(NEGATIVE_ONE_SPACE_COMPONENT.component), Math.round(pair.image.image.width.toDouble() * scale).toInt()) + NEW_LAYER
                parent.jsonArray?.let { array ->
                    HudImpl.createBit(shader, ascent) { y ->
                        array.add(JsonObject().apply {
                            addProperty("type", "bitmap")
                            addProperty("file", fileName)
                            addProperty("ascent", y)
                            addProperty("height", height)
                            add("chars", JsonArray().apply {
                                add(c)
                            })
                        })
                    }
                }
                ImageManager.setImage(shaderGroup, finalWidth)
                finalWidth
            }

            list.add(component.toPixelComponent(finalPixel.x + Math.round(pair.image.xOffset * scale).toInt()))
        }
        val renderer = ImageRenderer(
            hud,
            image.color,
            image.space,
            image.stack,
            image.maxStack,
            list,
            image.follow,
            image.cancelIfFollowerNotExists,
            image.conditions.and(image.image.conditions)
        )
        renderer.max() to renderer.getComponent(UpdateEvent.EMPTY)
    }

    val max = chars.first

    fun getComponent(hudPlayer: HudPlayer): PixelComponent = chars.second(hudPlayer, (hudPlayer.tick % Int.MAX_VALUE).toInt())

}