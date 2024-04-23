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
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.LayoutAnimationType
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.manager.TextManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class PopupLayout(
    private val layout: LayoutGroup,
    private val parent: PopupImpl,
    private val name: String,
    private val globalLocation: GuiLocation,
    private val globalPixel: ImageLocation,
    file: List<String>
) {
    private var imageChar = 0xCE000
    private var textIndex = 0

    private val imageEncoded = "popup_${parent.name}_image".encodeKey()
    private val imageKey = Key.key("$NAME_SPACE_ENCODED:popup/${parent.internalName}/$name/$imageEncoded")
    private val groups = parent.move.locations.run {
        val json = JsonArray()
        val textFolder = ArrayList(file).apply {
            add("text")
        }
        val map = map { location ->
            PopupLayoutGroup(location, json, textFolder)
        }
        PackGenerator.addTask(ArrayList(file).apply {
            add("$imageEncoded.json")
        }) {
            JsonObject().apply {
                add("providers", json)
            }.toByteArray()
        }
        map
    }
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int, Int) -> WidthComponent {
        val build = layout.conditions.build(reason)
        val map = groups.map {
            it.getComponent(reason)
        }
        return { player, index, frame ->
            if (build(player)) {
                if (index > map.lastIndex) {
                    EMPTY_WIDTH_COMPONENT
                } else {
                    val get = map[index](player, frame)
                    get[when (layout.animation.type) {
                        LayoutAnimationType.LOOP -> frame % get.size
                        LayoutAnimationType.PLAY_ONCE -> frame.coerceAtMost(get.lastIndex)
                    }]
                }
            } else EMPTY_WIDTH_COMPONENT
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray, textFolder: List<String>) {
        val elements = layout.animation.location.map { location ->
            PopupElement(pair, array, location, textFolder)
        }
        fun getComponent(reason: UpdateEvent): (HudPlayer, Int) -> List<WidthComponent> {
            val map = elements.map {
                it.getComponent(reason)
            }
            return { p, f ->
                map.map {
                    it(p, f)
                }
            }
        }
    }
    private inner class PopupElement(pair: LocationGroup, val array: JsonArray, location: ImageLocation, textFolder: List<String>) {
        private val elementGui = pair.gui + parent.gui + globalLocation
        private val elementPixel = globalPixel + location

        fun getComponent(reason: UpdateEvent): (HudPlayer, Int) -> WidthComponent {
            val imageProcessing = image.map {
                it.getComponent(reason)
            }
            val textProcessing = texts.map {
                it.getText(reason)
            }
            val headProcessing = heads.map {
                it.getHead(reason)
            }
            return { player, frame ->
                LayoutComponentContainer(layout.offset, layout.align, max)
                    .append(imageProcessing.map {
                        it(player, frame)
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
                elementGui,
                target.layer,
                target.outline
            )
            val pixel = elementPixel + pair.pixel + target.location
            val list = ArrayList<PixelComponent>()

            if (hudImage.listener != null) list.add(EMPTY_PIXEL_COMPONENT)
            if (hudImage.image.size > 1) hudImage.image.forEach {
                val char = (++imageChar).parseChar()
                val height = Math.round(it.image.image.height * target.scale).toInt()
                val scale = height.toDouble() / it.image.image.height

                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE_ENCODED:image/${hudImage.name}/${it.name}")
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
                    addProperty("file", "$NAME_SPACE_ENCODED:image/${it.name}")
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
            val pixel = elementPixel + pair.pixel + textLayout.location
            val textShader = HudShader(
                elementGui,
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
                val bit = HudImpl.createBit(pixel.y, textShader)
                textLayout.text.array.forEach {
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE_ENCODED:text/${textLayout.text.fontName}/${it.file}")
                        addProperty("ascent", bit)
                        addProperty("height", scale)
                        add("chars", it.chars)
                    })
                }
                var textIndex = 0xC0000
                val imageMap = HashMap<String, WidthComponent>()
                val textEncoded = "popup_${parent.name}_text_${index}".encodeKey()
                val key = Key.key("$NAME_SPACE_ENCODED:popup/${parent.internalName}/$name/text/$textEncoded")
                textLayout.text.images.forEach {
                    val result = (textIndex++).parseChar()
                    val imageScale = it.value.scale * textLayout.scale
                    val height = (it.value.image.image.height.toDouble() * imageScale).roundToInt()
                    val div = height.toDouble() / it.value.image.image.height
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE_ENCODED:text/${textLayout.text.fontName}/${"glyph_${it.key}".encodeKey()}.png")
                        addProperty("ascent", HudImpl.createBit(pixel.y + it.value.location.y, textShader))
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
                    textLayout.background?.let {
                        val y = HudImpl.createBit(pixel.y + it.location.y, HudShader(
                            elementGui,
                            textLayout.layer - 1,
                            false
                        ))
                        fun getString(image: LoadedImage, file: String): WidthComponent {
                            val result = (textIndex++).parseChar()
                            val height = (image.image.height.toDouble() * textLayout.backgroundScale).roundToInt()
                            val div = height.toDouble() / image.image.height
                            array.add(JsonObject().apply {
                                addProperty("type", "bitmap")
                                addProperty("file", "$NAME_SPACE_ENCODED:background/${it.name}/$file.png")
                                addProperty("ascent", y)
                                addProperty("height", height)
                                add("chars", JsonArray().apply {
                                    add(result)
                                })
                            })
                            return WidthComponent(Component.text().font(key).content(result).append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.image.width.toDouble() * div).roundToInt())
                        }
                        BackgroundLayout(
                            it.location.x,
                            getString(it.left, "background_${it.name}_left".encodeKey()),
                            getString(it.right, "background_${it.name}_right".encodeKey()),
                            getString(it.body, "background_${it.name}_body".encodeKey())
                        )
                    }
                )
                PackGenerator.addTask(ArrayList(textFolder).apply {
                    add("$textEncoded.json")
                }) {
                    JsonObject().apply {
                        add("providers", array)
                    }.toByteArray()
                }
                TextManager.setKey(group, result)
                result
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
            val pixel = elementPixel + pair.pixel + headLayout.location
            val shader = HudShader(
                elementGui,
                headLayout.layer,
                headLayout.outline
            )
            HeadRenderer(
                (0..7).map { i ->
                    val char = (++imageChar).parseChar()
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE_ENCODED:head/${"pixel_${headLayout.head.pixel}".encodeKey()}.png")
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