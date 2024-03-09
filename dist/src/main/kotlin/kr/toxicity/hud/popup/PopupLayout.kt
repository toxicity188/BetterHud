package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.hud.Hud
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.ListenerHudImage
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.image.SplitType
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.manager.TextManager
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import java.io.File
import kotlin.math.round

class PopupLayout(
    private val layout: LayoutGroup,
    private val parent: PopupImpl,
    private val name: String,
    private val globalLocation: GuiLocation,
    file: File
) {
    private var imageChar = 0xCE000
    private var textIndex = 0

    private val imageKey = Key.key("$NAME_SPACE:popup/${parent.name}/$name/image")
    private val groups = parent.move.locations.run {
        val json = JsonArray()
        val textFolder = file.subFolder("text")
        val map = map { location ->
            PopupLayoutGroup(location, json, textFolder)
        }
        JsonObject().apply {
            add("providers", json)
        }.save(File(file, "image.json"))
        map
    }

    fun getComponent(index: Int, animationIndex: Int, reason: UpdateEvent): (HudPlayer) -> WidthComponent {
        val get = groups[index % groups.size].getComponent(reason)
        return { player ->
            val list = get(player)
            list[animationIndex % list.size]
        }
    }
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int, Int) -> WidthComponent {
        val map = groups.map {
            it.getComponent(reason)
        }
        return { player, index, frame ->
            val get = map[index % map.size](player)
            get[frame % get.size]
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray, textFolder: File) {
        val elements = layout.animation.map { location ->
            PopupElement(pair, array, location, textFolder)
        }
        fun getComponent(animationIndex: Int, reason: UpdateEvent): (HudPlayer) -> WidthComponent {
            val get = elements[animationIndex % elements.size].getComponent(reason)
            return { p ->
                get(p)
            }
        }
        fun getComponent(reason: UpdateEvent): (HudPlayer) -> List<WidthComponent> {
            val map = elements.map {
                it.getComponent(reason)
            }
            return { p ->
                map.map {
                    it(p)
                }
            }
        }
    }
    private inner class PopupElement(pair: LocationGroup, val array: JsonArray, location: ImageLocation, textFolder: File) {
        private val gui = pair.gui + parent.gui + globalLocation

        fun getComponent(reason: UpdateEvent): (HudPlayer) -> WidthComponent {
            val imageProcessing = image.map {
                it.getComponent(reason)
            }
            val textProcessing = texts.map {
                it.getText(reason)
            }
            return { player ->
                LayoutComponentContainer()
                    .append(imageProcessing.map {
                        it(player)
                    })
                    .append(textProcessing.map {
                        it(player)
                    })
                    .build()
            }
        }

        val image = layout.image.map { target ->
            val hudImage = target.image
            val imageShader = HudShader(
                gui,
                target.layer,
                target.outline
            )
            val pixel = location + pair.pixel + target.location
            val list = ArrayList<PixelComponent>()

            if (hudImage is ListenerHudImage) list.add(EMPTY_PIXEL_COMPONENT)
            if (hudImage.image.size > 1) hudImage.image.forEach {
                val char = (++imageChar).parseChar()
                val height = round(it.image.image.height * target.scale).toInt()
                val scale = height.toDouble() / it.image.image.height

                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:image/${hudImage.name}/${it.name}")
                    addProperty("ascent", Hud.createBit(pixel.y, imageShader))
                    addProperty("height", height)
                    add("chars", JsonArray().apply {
                        add(char)
                    })
                })
                val xOffset = round(it.image.xOffset * scale).toInt()
                val xWidth = round(it.image.image.width.toDouble() * scale).toInt()
                val comp = WidthComponent(Component.text(char).font(imageKey), xWidth) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                list.add(comp.toPixelComponent(pixel.x + xOffset))
            } else hudImage.image[0].let {
                val char = (++imageChar).parseChar()
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:image/${it.name}")
                    addProperty("ascent", Hud.createBit(pixel.y, imageShader))
                    addProperty("height", round(it.image.image.height * target.scale).toInt())
                    add("chars", JsonArray().apply {
                        add(char)
                    })
                })
                val comp = WidthComponent(Component.text(char).font(imageKey), round(it.image.image.width.toDouble() * target.scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                list.add(comp.toPixelComponent(pixel.x))
            }

            ImageRenderer(
                hudImage,
                list,
                hudImage.conditions.and(target.conditions)
            )
        }
        val texts = layout.text.map { textLayout ->
            val pixel = location + pair.pixel + textLayout.location
            val textShader = HudShader(
                gui,
                textLayout.layer,
                textLayout.outline
            )
            val scale = round(textLayout.text.height.toDouble() * textLayout.scale).toInt()
            val group = ShaderGroup(textShader, textLayout.text.name, pixel.y)
            val textKey = TextManager.getKey(group) ?: run {
                val index = ++textIndex
                val array = JsonArray().apply {
                    add(JsonObject().apply {
                        addProperty("type", "space")
                        add("advances", JsonObject().apply {
                            addProperty(" ", 4)
                        })
                    })
                }
                textLayout.text.array.forEach {
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE:text/${textLayout.text.fontName}/${it.file}")
                        addProperty("ascent", Hud.createBit(pixel.y, textShader))
                        addProperty("height", scale)
                        add("chars", it.chars)
                    })
                }
                JsonObject().apply {
                    add("providers", array)
                }.save(File(textFolder, "text_${index}.json"))
                val key = Key.key("$NAME_SPACE:popup/${parent.name}/$name/text/text_${index}")
                TextManager.setKey(group, key)
                key
            }
            TextRenderer(
                textLayout.text.charWidth,
                Style.style(textLayout.color).font(textKey),
                textLayout.pattern,
                textLayout.align,
                scale.toDouble() / textLayout.text.height,
                pixel.x,
                textLayout.space,
                textLayout.numberEquation,
                textLayout.numberFormat,
                textLayout.conditions.and(textLayout.text.conditions)
            )
        }
    }
}