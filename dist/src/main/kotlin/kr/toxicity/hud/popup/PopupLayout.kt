package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.LocationGroup
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.EncodeManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType.FANCY
import kr.toxicity.hud.player.head.HeadRenderType.STANDARD
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.HudRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class PopupLayout(
    private val globalIndex: Int,
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

    fun getComponent(reason: UpdateEvent, frameSupplier: (HudPlayer) -> Long = { it.tick }): (HudPlayer, Int) -> Runner<WidthComponent> {
        val build = layout.conditions build reason
        val map = groups.map {
            it.getComponent(reason, frameSupplier).let { componentBuilder ->
                { player: HudPlayer ->
                    if (build(player)) componentBuilder(player) else Runner {
                        EMPTY_WIDTH_COMPONENT
                    }
                }
            }
        }
        return { player, index ->
            map[index](player)
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray) {
        private val elements = layout.animation.location.map { location ->
            PopupElement(pair, array, location)
        }
        fun getComponent(reason: UpdateEvent, frameSupplier: (HudPlayer) -> Long): (HudPlayer) -> Runner<WidthComponent> {
            return { p ->
                val frame = {
                    frameSupplier(p)
                }
                val m = elements.map {
                    it.getComponent(reason, frame)(p)
                }
                Runner {
                    layout.animation.type.choose(m, frame())()
                }
            }
        }
    }
    private inner class PopupElement(pair: LocationGroup, val array: JsonArray, location: PixelLocation) {
        private val elementGui = pair.gui + parent.gui + globalLocation
        private val elementPixel = globalPixel + location

        fun getComponent(reason: UpdateEvent, frameSupplier: () -> Long): (HudPlayer) -> Runner<WidthComponent> {
            val providers = renderers.map {
                it.render(reason)
            }
            return { player ->
                val result = providers.map {
                    it(player)
                }
                Runner {
                    val frame = frameSupplier()
                    LayoutComponentContainer(layout.offset, layout.align, max)
                        .append(result.map {
                            it(frame)
                        })
                        .build()
                }
            }
        }

        val image = layout.image.map { target ->
            val pixel = elementPixel + pair.pixel + target.location
            val imageShader = HudShader(
                elementGui,
                target.renderScale + pair.pixel + target.location,
                target.layer,
                target.outline,
                pixel.opacity,
                target.property
            )
            val negativeSpace = parent.getOrCreateSpace(-1)

            fun ImageElement.toComponent(parentComponent: ImageComponent? = null): ImageComponent {
                val list = ArrayList<PixelComponent>()
                if (listener != null) list.add(EMPTY_PIXEL_COMPONENT)
                image.forEach {
                    val fileName = "$NAME_SPACE_ENCODED:${it.name}"

                    val height = (it.image.image.height * target.scale * scale).roundToInt()
                    val scale = height.toDouble() / it.image.image.height
                    val xOffset = (it.image.xOffset * scale).roundToInt()
                    val ascent = pixel.y
                    val component = image(target.identifier(imageShader, ascent, fileName)) {
                        val char = parent.newChar
                        createAscent(imageShader, ascent) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            )
                        }
                        val xWidth = (it.image.image.width.toDouble() * scale).roundToInt()
                        val build = Component.text()
                            .font(parent.imageKey)
                        val comp = WidthComponent(
                            build.content("$char$negativeSpace"),
                            xWidth
                        )
                        comp
                    }
                    list += component.toPixelComponent(pixel.x + xOffset)
                }
                return ImageComponent(this, parentComponent, list, children.entries.associate {
                    it.key to it.value.toComponent()
                })
            }
            ImageRenderer(
                target,
                try {
                    target.source.toComponent()
                } catch (_: StackOverflowError) {
                    throw RuntimeException("circular reference found in ${target.source.id}")
                }
            )
        }

        private val max = image.maxOfOrNull {
            it.max()
        } ?: 0

        val texts = layout.text.map { textLayout ->
            val pixel = textLayout.location + elementPixel + pair.pixel
            val render = textLayout.renderScale + elementPixel + pair.pixel
            val textShader = HudShader(
                elementGui,
                render,
                textLayout.layer,
                textLayout.outline,
                pixel.opacity,
                textLayout.property
            )
            val scaledMap = textLayout.source.charWidth.intEntries.associate { (k, v) ->
                k to v * textLayout.scale
            }
            val scaledImageMap = textLayout.imageCharMap.intEntries.associate { (k, v) ->
                k to v * textLayout.scale * textLayout.emoji.scale
            }
            val index = ++textIndex
            val keys = (0..<textLayout.line).map { lineIndex ->
                text(textLayout.identifier(textShader, pixel.y + lineIndex * textLayout.lineWidth)) {
                    val array = textLayout.startJson()
                    textLayout.source.array.forEach {
                        createAscent(textShader, pixel.y + lineIndex * textLayout.lineWidth - it.ascent(textLayout.scale)) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to "$NAME_SPACE_ENCODED:${it.file}",
                                "ascent" to y,
                                "height" to (it.height * textLayout.scale).roundToInt(),
                                "chars" to it.chars
                            )
                        }
                    }
                    val textEncoded = "popup_${parent.name}_text_${globalIndex}_${index}_${lineIndex + 1}".encodeKey(EncodeManager.EncodeNamespace.FONT)
                    val key = createAdventureKey(textEncoded)
                    var imageTextIndex = TEXT_IMAGE_START_CODEPOINT + scaledImageMap.size
                    scaledImageMap.forEach { (k, v) ->
                        createAscent(textShader, pixel.y + v.location.y + lineIndex * textLayout.lineWidth + v.ascent) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to v.fileName,
                                "ascent" to y,
                                "height" to v.normalizedHeight,
                                "chars" to jsonArrayOf(k.parseChar())
                            )
                        }
                    }
                    PackGenerator.addTask(file + "$textEncoded.json") {
                        jsonObjectOf("providers" to array).toByteArray()
                    }
                    BackgroundKey(
                        key,
                        //TODO replace it to proper background in the future.
                        textLayout.background.source?.let {
                            fun getString(image: LoadedImage, file: String): WidthComponent {
                                val result = (++imageTextIndex).parseChar()
                                val height = (image.image.height.toDouble() * textLayout.background.scale).roundToInt()
                                val div = height.toDouble() / image.image.height
                                createAscent(textShader.toBackground(it.location.opacity), pixel.y + it.location.y + lineIndex * textLayout.lineWidth) { y ->
                                    array += jsonObjectOf(
                                        "type" to "bitmap",
                                        "file" to "$NAME_SPACE_ENCODED:$file.png",
                                        "ascent" to y,
                                        "height" to height,
                                        "chars" to jsonArrayOf(result)
                                    )
                                }
                                return WidthComponent(Component.text()
                                    .content(result)
                                    .append(NEGATIVE_ONE_SPACE_COMPONENT.finalizeFont().component), (image.image.width.toDouble() * div).roundToInt())
                            }
                            BackgroundLayout(
                                it.location.x,
                                getString(it.left, "background_${it.id}_left".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)),
                                getString(it.right, "background_${it.id}_right".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)),
                                getString(it.body, "background_${it.id}_body".encodeKey(EncodeManager.EncodeNamespace.TEXTURES))
                            )
                        }
                    )
                }
            }
            TextRenderer(
                textLayout,
                HudTextData(
                    keys,
                    (scaledMap.entries.associate { (k, v) ->
                        k to v.normalizedWidth
                    } + scaledImageMap.entries.associate { (k, v) ->
                        k to v.normalizedWidth
                    }).toIntMap(),
                    scaledImageMap.map {
                        it.value.name to it.key
                    }.toMap(),
                    textLayout.splitWidth
                ),
                pixel.x,
            )
        }

        val heads = layout.head.map { headLayout ->
            val pixel = headLayout.location + elementPixel + pair.pixel
            val render = headLayout.renderScale + elementPixel + pair.pixel
            val shader = HudShader(
                elementGui,
                render,
                headLayout.layer,
                headLayout.outline,
                pixel.opacity,
                headLayout.property
            )
            HeadRenderer(
                headLayout,
                parent.getOrCreateSpace(-1),
                parent.getOrCreateSpace(-(headLayout.source.pixel * 8 + 1)),
                parent.getOrCreateSpace(-(headLayout.source.pixel + 1)),
                (0..7).map { i ->
                    val encode = "pixel_${headLayout.source.pixel}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)
                    val fileName = "$NAME_SPACE_ENCODED:$encode.png"
                    val char = parent.newChar
                    val ascent = pixel.y + i * headLayout.source.pixel
                    val height = headLayout.source.pixel
                    val mainChar = head(headLayout.identifier(shader, ascent, fileName)) {
                        createAscent(shader, ascent) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            )
                        }
                        char
                    }
                    when (headLayout.type) {
                        STANDARD -> HeadKey(mainChar, mainChar)
                        FANCY -> {
                            val fancy = shader.toFancyHead()
                            HeadKey(
                                mainChar,
                                head(headLayout.identifier(fancy, ascent - headLayout.source.pixel, fileName)) {
                                    val twoChar = parent.newChar
                                    createAscent(fancy, ascent - headLayout.source.pixel) { y ->
                                        array += jsonObjectOf(
                                            "type" to "bitmap",
                                            "file" to fileName,
                                            "ascent" to y,
                                            "height" to height,
                                            "chars" to jsonArrayOf(twoChar)
                                        )
                                    }
                                    twoChar
                                }
                            )
                        }
                    }
                },
                parent.imageKey,
                headLayout.source.pixel * 8,
                pixel.x
            )
        }

        private val renderers: List<HudRenderer> = listOf(
            image,
            texts,
            heads
        ).sum()
    }
}