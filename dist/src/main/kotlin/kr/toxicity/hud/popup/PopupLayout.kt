package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.layout.LayoutAnimationType
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.manager.TextManager
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.io.File

class PopupLayout(
    private val layout: LayoutGroup,
    private val parent: PopupImpl,
    private val name: String,
    private val globalLocation: GuiLocation,
    file: File
) {
    private var imageChar = 0xCE000
    private var textIndex = 0

    private val imageKey = Key.key("$NAME_SPACE:popup/${parent.internalName}/$name/image")
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
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int, Int) -> WidthComponent {
        val map = groups.map {
            it.getComponent(reason)
        }
        return { player, index, frame ->
            if (index > map.lastIndex) {
                EMPTY_WIDTH_COMPONENT
            } else {
                val get = map[index](player)
                get[when (layout.animation.type) {
                    LayoutAnimationType.LOOP -> frame % get.size
                    LayoutAnimationType.PLAY_ONCE -> frame.coerceAtMost(get.lastIndex)
                }]
            }
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray, textFolder: File) {
        val elements = layout.animation.location.map { location ->
            PopupElement(pair, array, location, textFolder)
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
            val headProcessing = heads.map {
                it.getHead(reason)
            }
            return { player ->
                LayoutComponentContainer(layout.align, max)
                    .append(imageProcessing.map {
                        it(player)
                    })
                    .append(textProcessing.map {
                        it(player)
                    })
                    .append(headProcessing.map {
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

            if (hudImage.listener != null) list.add(EMPTY_PIXEL_COMPONENT)
            if (hudImage.image.size > 1) hudImage.image.forEach {
                val char = (++imageChar).parseChar()
                val height = Math.round(it.image.image.height * target.scale).toInt()
                val scale = height.toDouble() / it.image.image.height

                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:image/${hudImage.name}/${it.name}")
                    addProperty("ascent", HudImpl.createBit(pixel.y, imageShader))
                    addProperty("height", height)
                    add("chars", JsonArray().apply {
                        add(char)
                    })
                })
                val xOffset = Math.round(it.image.xOffset * scale).toInt()
                val xWidth = Math.round(it.image.image.width.toDouble() * scale).toInt()
                val comp = WidthComponent(Component.text().content(char).font(imageKey), xWidth) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                list.add(comp.toPixelComponent(pixel.x + xOffset))
            } else hudImage.image[0].let {
                val char = (++imageChar).parseChar()
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE:image/${it.name}")
                    addProperty("ascent", HudImpl.createBit(pixel.y, imageShader))
                    addProperty("height", Math.round(it.image.image.height * target.scale).toInt())
                    add("chars", JsonArray().apply {
                        add(char)
                    })
                })
                val comp = WidthComponent(Component.text().content(char).font(imageKey), Math.round(it.image.image.width.toDouble() * target.scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                list.add(comp.toPixelComponent(pixel.x))
            }

            ImageRenderer(
                hudImage,
                target.color,
                list,
                hudImage.conditions.and(target.conditions)
            )
        }

        private val max = image.maxOfOrNull {
            it.max()
        } ?: 0

        val texts = layout.text.map { textLayout ->
            val pixel = location + pair.pixel + textLayout.location
            val textShader = HudShader(
                gui,
                textLayout.layer,
                textLayout.outline
            )
            val scale = Math.round(textLayout.text.height.toDouble() * textLayout.scale).toInt()
            val group = ShaderGroup(textShader, textLayout.text.name, scale, pixel.y)
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
                        addProperty("ascent", HudImpl.createBit(pixel.y, textShader))
                        addProperty("height", scale)
                        add("chars", it.chars)
                    })
                }
                JsonObject().apply {
                    add("providers", array)
                }.save(File(textFolder, "text_${index}.json"))
                val key = Key.key("$NAME_SPACE:popup/${parent.internalName}/$name/text/text_${index}")
                TextManager.setKey(group, key)
                key
            }
            TextRenderer(
                textLayout.text.charWidth,
                textLayout.color,
                textKey,
                textLayout.pattern,
                textLayout.align,
                scale.toDouble() / textLayout.text.height.toDouble(),
                pixel.x,
                textLayout.deserializeText,
                textLayout.space,
                textLayout.numberEquation,
                textLayout.numberFormat,
                textLayout.conditions.and(textLayout.text.conditions)
            )
        }

        val heads = layout.head.map { headLayout ->
            val pixel = location + pair.pixel + headLayout.location
            val shader = HudShader(
                gui,
                headLayout.layer,
                headLayout.outline
            )
            HeadRenderer(
                (0..7).map { i ->
                    val char = (++imageChar).parseChar()
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE:head/pixel_${headLayout.head.pixel}.png")
                        addProperty("ascent", HudImpl.createBit(pixel.y + i * headLayout.head.pixel, shader))
                        addProperty("height", headLayout.head.pixel)
                        add("chars", JsonArray().apply {
                            add(char)
                        })
                    })
                    Component.text().content(char).font(imageKey)
                },
                headLayout.head.pixel,
                pixel.x,
                headLayout.align,
                headLayout.conditions.and(headLayout.head.conditions)
            )
        }
    }
}