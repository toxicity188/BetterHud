package kr.toxicity.hud.renderer.bake

import kr.toxicity.hud.animation.AnimationFrame
import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.element.ImageElement
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.EncodeManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType
import kr.toxicity.hud.renderer.HeadRenderer
import kr.toxicity.hud.renderer.ImageRenderer
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class ElementBakery(
    private val host: ElementHost,
    private val fontDir: List<String>,
    private val gui: GuiLocation,
    private val layoutIndex: Int
) {
    companion object {
        const val TEXT_BASE_HEIGHT = 8.0
    }

    private fun Int.clampAscent() = coerceIn(-HUD_ADD_HEIGHT, HUD_ADD_HEIGHT)

    private fun HudLayout<*>.shader(base: PixelLocation, frame: AnimationFrame, pixel: PixelLocation) = HudShader(
        gui,
        renderScale + base + frame.pixel,
        layer,
        outline,
        pixel.opacity,
        property
    )

    fun image(layout: ImageLayout, base: PixelLocation, frame: AnimationFrame): ImageRenderer {
        val pixel = layout.location + base + frame.pixel
        val shader = layout.shader(base, frame, pixel)
        val negativeSpace = host.getOrCreateSpace(-1)
        val frameScale = layout.scale * frame.scale

        fun ImageElement.toComponent(parentComponent: ImageComponent? = null): ImageComponent {
            val list = ArrayList<PixelComponent>()
            if (listener != null) list.add(EMPTY_PIXEL_COMPONENT)
            image.forEach { pair ->
                val fileName = "$NAME_SPACE_ENCODED:${pair.name}"
                val original = pair.image.image
                val height = (original.height * frameScale * scale).roundToInt()
                val ratio = height.toDouble() / original.height
                val baseRatio = layout.scale * scale
                val pivot = layout.pivot.offset(original.width * baseRatio, original.height * baseRatio, frame.scale)
                val ascent = (pixel.y + pivot.y).clampAscent()
                val component = image(layout.identifier(shader, ascent, fileName, frameScale)) {
                    val char = host.newChar
                    host.json?.let { array ->
                        createAscent(shader, ascent) { y ->
                            array += jsonObjectOf(
                                "type" to "bitmap",
                                "file" to fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(char)
                            )
                        }
                    }
                    WidthComponent(
                        Component.text().font(host.imageKey).content("$char$negativeSpace"),
                        (original.width * ratio).roundToInt()
                    )
                }
                list += component.toPixelComponent(pixel.x + pivot.x + (pair.image.xOffset * ratio).roundToInt())
            }
            return ImageComponent(this, parentComponent, list, children.entries.associate {
                it.key to it.value.toComponent()
            })
        }
        return ImageRenderer(
            layout,
            try {
                layout.source.toComponent()
            } catch (_: StackOverflowError) {
                throw RuntimeException("circular reference found in ${layout.source.id}")
            }
        )
    }

    fun text(layout: TextLayout, base: PixelLocation, frame: AnimationFrame): TextRenderer {
        val pixel = layout.location + base + frame.pixel
        val shader = layout.shader(base, frame, pixel)
        val scale = layout.scale * frame.scale
        val pivotY = layout.pivot.offset(0.0, TEXT_BASE_HEIGHT * layout.scale, frame.scale).y
        val scaledMap = layout.source.charWidth.intEntries.associate { (k, v) ->
            k to v * scale
        }
        val scaledImageMap = layout.imageCharMap.intEntries.associate { (k, v) ->
            k to v * scale * layout.emoji.scale
        }
        val textIndex = host.nextTextIndex()
        val keys = (0..<layout.line).map { lineIndex ->
            val lineY = (pixel.y + pivotY + lineIndex * layout.lineWidth).clampAscent()
            text(layout.identifier(shader, lineY, scale)) {
                val array = layout.startJson()
                layout.source.array.forEach {
                    createAscent(shader, lineY - it.ascent(scale)) { y ->
                        array += jsonObjectOf(
                            "type" to "bitmap",
                            "file" to "$NAME_SPACE_ENCODED:${it.file}",
                            "ascent" to y,
                            "height" to (it.height * scale).roundToInt(),
                            "chars" to it.chars
                        )
                    }
                }
                val textEncoded = "${host.hostKind}_${host.hostName}_text_${layoutIndex}_${textIndex}_${lineIndex + 1}".encodeKey(EncodeManager.EncodeNamespace.FONT)
                val key = createAdventureKey(textEncoded)
                var imageTextIndex = TEXT_IMAGE_START_CODEPOINT + scaledImageMap.size
                scaledImageMap.forEach { (k, v) ->
                    createAscent(shader, lineY + v.location.y + v.ascent) { y ->
                        array += jsonObjectOf(
                            "type" to "bitmap",
                            "file" to v.fileName,
                            "ascent" to y,
                            "height" to v.normalizedHeight,
                            "chars" to jsonArrayOf(k.parseChar())
                        )
                    }
                }
                PackGenerator.addTask(fontDir + "$textEncoded.json") {
                    jsonObjectOf("providers" to array).toByteArray()
                }
                BackgroundKey(
                    key,
                    layout.background.source?.let {
                        fun getString(image: LoadedImage, file: String): WidthComponent {
                            val result = (++imageTextIndex).parseChar()
                            val height = (image.image.height.toDouble() * layout.background.scale).roundToInt()
                            val div = height.toDouble() / image.image.height
                            createAscent(shader.toBackground(it.location.opacity), lineY + it.location.y) { y ->
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
        return TextRenderer(
            layout,
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
                layout.splitWidth
            ),
            pixel.x
        )
    }

    fun head(layout: HeadLayout, base: PixelLocation, frame: AnimationFrame): HeadRenderer {
        val pixel = layout.location + base + frame.pixel
        val shader = layout.shader(base, frame, pixel)
        val size = layout.source.pixel
        val encode = "pixel_$size".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)
        val fileName = "$NAME_SPACE_ENCODED:$encode.png"

        fun bakeRow(rowShader: HudShader, ascent: Int) = head(layout.identifier(rowShader, ascent, fileName)) {
            val char = host.newChar
            host.json?.let { array ->
                createAscent(rowShader, ascent) { y ->
                    array += jsonObjectOf(
                        "type" to "bitmap",
                        "file" to fileName,
                        "ascent" to y,
                        "height" to size,
                        "chars" to jsonArrayOf(char)
                    )
                }
            }
            char
        }
        return HeadRenderer(
            layout,
            host.getOrCreateSpace(-1),
            host.getOrCreateSpace(-(size * 8 + 1)),
            host.getOrCreateSpace(-(size + 1)),
            (0..7).map { i ->
                val ascent = (pixel.y + i * size).clampAscent()
                val main = bakeRow(shader, ascent)
                when (layout.type) {
                    HeadRenderType.STANDARD -> HeadKey(main, main)
                    HeadRenderType.FANCY -> HeadKey(main, bakeRow(shader.toFancyHead(), ascent - size))
                }
            },
            host.imageKey,
            size * 8,
            pixel.x
        )
    }
}
