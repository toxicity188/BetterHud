package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.location.AnimationType
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType.FANCY
import kr.toxicity.hud.player.head.HeadRenderType.STANDARD
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class PopupLayout(
    private val json: JsonArray,
    private val layout: LayoutGroup,
    private val parent: PopupImpl,
    private val globalLocation: GuiLocation,
    private val globalPixel: PixelLocation,
    private val file: List<String>
) {
    private var textIndex = 0

    private val groups = parent.move.locations.map { location ->
        PopupLayoutGroup(location, json)
    }

    fun getComponent(reason: UpdateEvent): (HudPlayer, Int, Int) -> WidthComponent {
        val build = layout.conditions.build(reason)
        val map = groups.map {
            it.getComponent(reason)
        }
        return { hudPlayer, index, frame ->
            if (build(hudPlayer)) {
                if (index > map.lastIndex) {
                    EMPTY_WIDTH_COMPONENT
                } else {
                    val get = map[index](hudPlayer, frame)
                    get[when (layout.animation.type) {
                        AnimationType.LOOP -> frame % get.size
                        AnimationType.PLAY_ONCE -> frame.coerceAtMost(get.lastIndex)
                    }]
                }
            } else EMPTY_WIDTH_COMPONENT
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray) {
        val elements = layout.animation.location.map { location ->
            PopupElement(pair, array, location)
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
    private inner class PopupElement(pair: LocationGroup, val array: JsonArray, location: PixelLocation) {
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
            return { hudPlayer, frame ->
                LayoutComponentContainer(layout.offset, layout.align, max)
                    .append(imageProcessing.map {
                        it(hudPlayer, frame)
                    })
                    .append(textProcessing.map {
                        it(hudPlayer)
                    })
                    .append(headProcessing.map {
                        it(hudPlayer)
                    })
                    .build()
            }
        }

        val image = layout.image.map { target ->
            val hudImage = target.image
            val pixel = elementPixel + pair.pixel + target.location
            val imageShader = HudShader(
                elementGui,
                target.renderScale,
                target.layer,
                target.outline,
                pixel.opacity,
                target.property
            )
            val list = ArrayList<PixelComponent>()

            if (hudImage.listener != null) list.add(EMPTY_PIXEL_COMPONENT)
            if (hudImage.image.size > 1) hudImage.image.forEach {
                val fileName = "$NAME_SPACE_ENCODED:${it.name}"

                val height = Math.round(it.image.image.height * target.scale).toInt()
                val scale = height.toDouble() / it.image.image.height
                val xOffset = Math.round(it.image.xOffset * scale).toInt()
                val ascent = pixel.y
                val shaderGroup = ShaderGroup(imageShader, fileName, target.scale, ascent)

                val component = ImageManager.getImage(shaderGroup) ?: run {
                    val char = parent.newChar()
                    HudImpl.createBit(imageShader, ascent) { y ->
                        array.add(jsonObjectOf(
                            "type" to "bitmap",
                            "file" to fileName,
                            "ascent" to y,
                            "height" to height,
                            "chars" to jsonArrayOf(char)
                        ))
                    }
                    val xWidth = Math.round(it.image.image.width.toDouble() * scale).toInt()
                    val comp = WidthComponent(Component.text().content(char).font(parent.imageKey), xWidth) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                    ImageManager.setImage(shaderGroup, comp)
                    comp
                }

                list.add(component.toPixelComponent(pixel.x + xOffset))
            } else hudImage.image[0].let {
                val char = parent.newChar()
                HudImpl.createBit(imageShader, pixel.y) { y ->
                    array.add(jsonObjectOf(
                        "type" to "bitmap",
                        "file" to "$NAME_SPACE_ENCODED:${it.name}",
                        "ascent" to y,
                        "height" to (it.image.image.height * target.scale).roundToInt(),
                        "chars" to jsonArrayOf(char)
                    ))
                }
                val comp = WidthComponent(Component.text().content(char).font(parent.imageKey), Math.round(it.image.image.width.toDouble() * target.scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                list.add(comp.toPixelComponent(pixel.x))
            }

            ImageRenderer(
                hudImage,
                target.color,
                target.space,
                target.stack,
                target.maxStack,
                list,
                target.follow,
                target.cancelIfFollowerNotExists,
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
                textLayout.renderScale,
                textLayout.layer,
                textLayout.outline,
                pixel.opacity,
                textLayout.property
            )
            val group = ShaderGroup(textShader, textLayout.text.name, textLayout.scale, pixel.y)
            val textKey = TextManagerImpl.getKey(group) ?: run {
                val index = ++textIndex
                val array = textLayout.startJson()
                HudImpl.createBit(textShader, pixel.y) { y ->
                    textLayout.text.array.forEach {
                        array.add(jsonObjectOf(
                            "type" to "bitmap",
                            "file" to "$NAME_SPACE_ENCODED:${it.file}",
                            "ascent" to y,
                            "height" to (it.height * textLayout.scale).roundToInt(),
                            "chars" to it.chars
                        ))
                    }
                }
                var textIndex = 0xC0000
                val imageMap = HashMap<String, WidthComponent>()
                val textEncoded = "popup_${parent.name}_text_${index}".encodeKey()
                val key = createAdventureKey(textEncoded)
                textLayout.text.images.forEach {
                    val result = textIndex++.parseChar()
                    val imageScale = it.value.scale * textLayout.scale
                    val height = (it.value.image.image.height.toDouble() * imageScale).roundToInt()
                    val div = height.toDouble() / it.value.image.image.height
                    HudImpl.createBit(textShader, pixel.y + it.value.location.y) { y ->
                        array.add(jsonObjectOf(
                            "type" to "bitmap",
                            "file" to "$NAME_SPACE_ENCODED:${"glyph_${it.key}".encodeKey()}.png",
                            "ascent" to y,
                            "height" to height,
                            "chars" to jsonArrayOf(result)
                        ))
                    }
                    imageMap[it.key] = it.value.location.x.toSpaceComponent() + WidthComponent(Component.text()
                        .font(key)
                        .content(result)
                        .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (it.value.image.image.width.toDouble() * div).roundToInt())
                }
                if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
                    HudImpl.createBit(textShader, textLayout.emojiLocation.y) { y ->
                        MinecraftManager.applyAll(array, y, textLayout.emojiScale, key) {
                            textIndex++
                        }.forEach {
                            imageMap[it.key] = textLayout.emojiLocation.x.toSpaceComponent() + it.value
                        }
                    }
                }
                val result = HudTextData(
                    key,
                    imageMap,
                    textLayout.background?.let {
                        val backgroundLoc = pixel + it.location
                        fun getString(image: LoadedImage, file: String): WidthComponent {
                            val result = textIndex++.parseChar()
                            val height = (image.image.height.toDouble() * textLayout.backgroundScale).roundToInt()
                            val div = height.toDouble() / image.image.height
                            HudImpl.createBit(HudShader(
                                elementGui,
                                textLayout.renderScale,
                                textLayout.layer - 1,
                                false,
                                backgroundLoc.opacity,
                                textLayout.property
                            ), backgroundLoc.y) { y ->
                                array.add(jsonObjectOf(
                                    "type" to "bitmap",
                                    "file" to "$NAME_SPACE_ENCODED:$file.png",
                                    "ascent" to y,
                                    "height" to height,
                                    "chars" to jsonArrayOf(result)
                                ))
                            }
                            return WidthComponent(Component.text().font(key).content(result).append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.image.width.toDouble() * div).roundToInt())
                        }
                        BackgroundLayout(
                            backgroundLoc.x,
                            getString(it.left, "background_${it.name}_left".encodeKey()),
                            getString(it.right, "background_${it.name}_right".encodeKey()),
                            getString(it.body, "background_${it.name}_body".encodeKey())
                        )
                    }
                )
                PackGenerator.addTask(file + "$textEncoded.json") {
                    jsonObjectOf("providers" to array).toByteArray()
                }
                TextManagerImpl.setKey(group, result)
                result
            }
            TextRenderer(
                textLayout.text.charWidth,
                textLayout.color,
                textKey,
                textLayout.pattern,
                textLayout.align,
                textLayout.scale,
                pixel.x,
                textLayout.numberEquation,
                textLayout.numberFormat,
                textLayout.disableNumberFormat,
                textLayout.follow,
                textLayout.cancelIfFollowerNotExists,
                textLayout.useLegacyFormat,
                textLayout.legacySerializer,
                textLayout.space,
                textLayout.conditions.and(textLayout.text.conditions)
            )
        }

        val heads = layout.head.map { headLayout ->
            val pixel = elementPixel + pair.pixel + headLayout.location
            val shader = HudShader(
                elementGui,
                headLayout.renderScale,
                headLayout.layer,
                headLayout.outline,
                pixel.opacity,
                headLayout.property
            )
            val hair = when (headLayout.type) {
                STANDARD -> shader
                FANCY -> HudShader(
                    elementGui,
                    headLayout.renderScale * 1.125,
                    headLayout.layer + 1,
                    true,
                    pixel.opacity,
                    headLayout.property
                )
            }
            HeadRenderer(
                parent.getOrCreateSpace(-1),
                parent.getOrCreateSpace(-(headLayout.head.pixel * 8 + 1)),
                parent.getOrCreateSpace(-(headLayout.head.pixel + 1)),
                (0..7).map { i ->
                    val encode = "pixel_${headLayout.head.pixel}".encodeKey()
                    val fileName = "$NAME_SPACE_ENCODED:$encode.png"
                    val char = parent.newChar()
                    val ascent = pixel.y + i * headLayout.head.pixel
                    val height = headLayout.head.pixel
                    val shaderGroup = ShaderGroup(shader, fileName, 1.0, ascent)

                    val mainChar = PlayerHeadManager.getHead(shaderGroup) ?: run {
                        HudImpl.createBit(shader, ascent) { y ->
                            array.add(jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            ))
                        }
                        PlayerHeadManager.setHead(shaderGroup, char)
                        char
                    }
                    when (headLayout.type) {
                        STANDARD -> HeadKey(mainChar, mainChar)
                        FANCY -> {
                            val hairShaderGroup = ShaderGroup(hair, fileName, 1.0, ascent - headLayout.head.pixel)
                            HeadKey(
                                mainChar,
                                PlayerHeadManager.getHead(hairShaderGroup) ?: run {
                                    val twoChar = parent.newChar()
                                    HudImpl.createBit(hair, ascent - headLayout.head.pixel) { y ->
                                        array.add(jsonObjectOf(
                                            "type" to "bitmap",
                                            "file" to fileName,
                                            "ascent" to y,
                                            "height" to height,
                                            "chars" to jsonArrayOf(twoChar)
                                        ))
                                    }
                                    PlayerHeadManager.setHead(hairShaderGroup, twoChar)
                                    twoChar
                                }
                            )
                        }
                    }
                },
                parent.imageKey,
                headLayout.head.pixel * 8,
                pixel.x,
                headLayout.align,
                headLayout.type,
                headLayout.follow,
                headLayout.cancelIfFollowerNotExists,
                headLayout.conditions.and(headLayout.head.conditions)
            )
        }
    }
}