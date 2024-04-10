package kr.toxicity.hud.hud

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.manager.TextManager
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.io.File
import kotlin.math.roundToInt

class HudTextElement(parent: HudImpl, name: String, file: File, private val text: TextLayout, index: Int, gui: GuiLocation, pixel: ImageLocation) {

    private val renderer = run {
        val shader = HudShader(
            gui,
            text.layer,
            text.outline
        )
        val loc = text.location + pixel
        val yAxis = (loc.y).coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
        val scale = Math.round(text.text.height * text.scale).toInt()
        val group = ShaderGroup(shader, text.text.name, scale, yAxis)
        val key = TextManager.getKey(group) ?: run {
            val index2 = (++parent.textIndex)
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
                    addProperty("ascent", HudImpl.createBit(yAxis, shader))
                    addProperty("height", scale)
                    add("chars", it.chars)
                })
            }
            var textIndex = 0xC0000
            val key = Key.key("$NAME_SPACE:hud/$name/text/text_${index + 1}_${index2 + 1}")
            val imageMap = HashMap<String, WidthComponent>()
            text.text.images.forEach {
                val result = (textIndex++).parseChar()
                val imageScale = it.value.scale * text.scale
                val height = (it.value.image.image.height.toDouble() * imageScale).roundToInt()
                val div = height.toDouble() / it.value.image.image.height
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:text/${text.text.fontName}/image_${it.key}.png")
                    addProperty("ascent", HudImpl.createBit(pixel.y + it.value.location.y, shader))
                    addProperty("height", height)
                    add("chars", JsonArray().apply {
                        add(result)
                    })
                })
                imageMap[it.key] = it.value.location.x.toSpaceComponent() + WidthComponent(Component.text()
                    .font(key)
                    .content(result)
                    .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (it.value.image.image.width.toDouble() * div).roundToInt())
            }
            val result = HudTextData(
                key,
                imageMap,
                text.background?.let {
                    val y = HudImpl.createBit(pixel.y + it.location.y, HudShader(
                        gui,
                        text.layer - 1,
                        false
                    ))
                    fun getString(image: LoadedImage, file: String): WidthComponent {
                        val result = (textIndex++).parseChar()
                        val height = (image.image.height.toDouble() * text.backgroundScale).roundToInt()
                        val div = height.toDouble() / image.image.height
                        array.add(JsonObject().apply {
                            addProperty("type", "bitmap")
                            addProperty("file", "$NAME_SPACE:background/${it.name}/$file.png")
                            addProperty("ascent", y)
                            addProperty("height", height)
                            add("chars", JsonArray().apply {
                                add(result)
                            })
                        })
                        return it.location.x.toSpaceComponent() + WidthComponent(Component.text().font(key).content(result).append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.image.width.toDouble() * div).roundToInt())
                    }
                    BackgroundLayout(
                        getString(it.left, "left"),
                        getString(it.right, "right"),
                        getString(it.body, "body")
                    )
                }
            )
            JsonObject().apply {
                add("providers", array)
            }.save(File(file, "text_${index + 1}_${index2 + 1}.json"))
            TextManager.setKey(group, result)
            result
        }
        TextRenderer(
            text.text.charWidth,
            text.color,
            key,
            text.pattern,
            text.align,
            scale.toDouble() / text.text.height.toDouble(),
            loc.x,
            text.deserializeText,
            text.space,
            text.numberEquation,
            text.numberFormat,
            text.conditions.and(text.text.conditions)
        )
    }.getText(UpdateEvent.EMPTY)

    fun getText(player: HudPlayer): PixelComponent = renderer(player)
}