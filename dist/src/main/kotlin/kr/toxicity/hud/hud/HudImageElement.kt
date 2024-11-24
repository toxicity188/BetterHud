package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.manager.ImageManager
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class HudImageElement(parent: HudImpl, private val imageLayout: ImageLayout, gui: GuiLocation, pixel: PixelLocation) {

    private val chars = run {
        val finalPixel = imageLayout.location + pixel

        val shader = HudShader(
            gui,
            imageLayout.renderScale,
            imageLayout.layer,
            imageLayout.outline,
            finalPixel.opacity,
            imageLayout.property
        )
        val negativeSpace = parent.getOrCreateSpace(-1)
        fun HudImage.toComponent(parentComponent: ImageComponent? = null): ImageComponent {
            val list = ArrayList<PixelComponent>()
            if (listener != null) {
                list.add(EMPTY_PIXEL_COMPONENT)
            }
            image.forEach { pair ->
                val fileName = "$NAME_SPACE_ENCODED:${pair.name}"
                val height = (pair.image.image.height.toDouble() * imageLayout.scale).roundToInt()
                val scale = height.toDouble() / pair.image.image.height
                val ascent = finalPixel.y.coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
                val shaderGroup = ShaderGroup(shader, fileName, imageLayout.scale, ascent)

                val component = ImageManager.getImage(shaderGroup) ?: run {
                    val c = (++parent.imageChar).parseChar()
                    val comp = Component.text()
                        .font(parent.imageKey)
                    val finalWidth = WidthComponent(
                        if (BOOTSTRAP.useLegacyFont()) comp.content(c).append(NEGATIVE_ONE_SPACE_COMPONENT.component) else comp.content("$c$negativeSpace"),
                        (pair.image.image.width.toDouble() * scale).roundToInt()
                    )
                    parent.jsonArray?.let { array ->
                        HudImpl.createBit(shader, ascent) { y ->
                            array.add(jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(c)
                            ))
                        }
                    }
                    ImageManager.setImage(shaderGroup, finalWidth)
                    finalWidth
                }

                list.add(component.toPixelComponent(finalPixel.x + (pair.image.xOffset * scale).roundToInt()))
            }
            return ImageComponent(this, parentComponent, list, children.entries.associate {
                it.key to it.value.toComponent()
            })
        }
        val renderer = ImageRenderer(
            imageLayout.color,
            imageLayout.space,
            imageLayout.stack,
            imageLayout.maxStack,
            try {
                imageLayout.image.toComponent()
            } catch (_: StackOverflowError) {
                throw RuntimeException("circular reference found in ${imageLayout.image.name}")
            },
            imageLayout.follow,
            imageLayout.cancelIfFollowerNotExists,
            imageLayout.conditions and imageLayout.image.conditions
        )
        renderer.max() to renderer.getComponent(UpdateEvent.EMPTY)
    }

    val max = chars.first

    fun getComponent(hudPlayer: HudPlayer): PixelComponent = chars.second(hudPlayer, (hudPlayer.tick % Int.MAX_VALUE).toInt())

}