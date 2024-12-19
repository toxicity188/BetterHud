package kr.toxicity.hud.hud

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.LoadedImage
import kr.toxicity.hud.layout.BackgroundLayout
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.EncodeManager
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class HudTextParser(
    globalIndex: Int,
    parent: HudImpl,
    resource: GlobalResource,
    private val text: TextLayout,
    gui: GuiLocation,
    pixel: PixelLocation
) : HudSubParser {

    private val renderer = run {
        val loc = text.location + pixel
        val render = text.renderScale + pixel
        val shader = HudShader(
            gui,
            render,
            text.layer,
            text.outline,
            loc.opacity,
            text.property
        )
        val scaledMap = text.source.charWidth.entries.associate { (k, v) ->
            k to v * text.scale
        }
        val scaledImageMap = text.imageCharMap.entries.associate { (k, v) ->
            k to v * text.scale * text.emoji.scale
        }
        val index2 = ++parent.textIndex
        val keys = (0..<text.line).map { lineIndex ->
            val yAxis = (loc.y + lineIndex * text.lineWidth).coerceAtLeast(-HUD_ADD_HEIGHT).coerceAtMost(HUD_ADD_HEIGHT)
            text(text.identifier(shader, yAxis)) {
                val array = text.startJson()
                text.source.array.forEach {
                    createAscent(shader, yAxis) { y ->
                        array += jsonObjectOf(
                            "type" to "bitmap",
                            "file" to "$NAME_SPACE_ENCODED:${it.file}",
                            "ascent" to y,
                            "height" to (it.height * text.scale).roundToInt(),
                            "chars" to it.chars
                        )
                    }
                }
                var textIndex = TEXT_IMAGE_START_CODEPOINT + scaledImageMap.size
                val textEncoded = "hud_${parent.name}_text_${globalIndex}_${index2 + 1}_${lineIndex + 1}".encodeKey(EncodeManager.EncodeNamespace.FONT)
                val key = createAdventureKey(textEncoded)
                scaledImageMap.forEach { (k, v) ->
                    createAscent(shader, loc.y + v.location.y + lineIndex * text.lineWidth + v.ascent) { y ->
                        array += jsonObjectOf(
                            "type" to "bitmap",
                            "file" to v.fileName,
                            "ascent" to y,
                            "height" to v.normalizedHeight,
                            "chars" to jsonArrayOf(k.parseChar())
                        )
                    }
                }
                PackGenerator.addTask(resource.font + "$textEncoded.json") {
                    jsonObjectOf("providers" to array).toByteArray()
                }
                BackgroundKey(
                    key,
                    //TODO replace it to proper background in the future.
                    text.background.source?.let {
                        fun getString(image: LoadedImage, file: String): WidthComponent {
                            val result = (++textIndex).parseChar()
                            val height = (image.image.height.toDouble() * text.background.scale).roundToInt()
                            val div = height.toDouble() / image.image.height
                            createAscent(HudShader(
                                gui,
                                render,
                                text.layer - 1,
                                false,
                                loc.opacity * it.location.opacity,
                                text.property
                            ), loc.y + it.location.y + lineIndex * text.lineWidth) { y ->
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
                            getString(it.left, "background_${it.name}_left".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)),
                            getString(it.right, "background_${it.name}_right".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)),
                            getString(it.body, "background_${it.name}_body".encodeKey(EncodeManager.EncodeNamespace.TEXTURES))
                        )
                    }
                )
            }
        }
        TextRenderer(
            text,
            HudTextData(
                keys,
                scaledMap.entries.associate { (k, v) ->
                    k to v.normalizedWidth
                } + scaledImageMap.entries.associate { (k, v) ->
                    k to v.normalizedWidth
                },
                scaledImageMap.map {
                    it.value.name to it.key
                }.toMap(),
                text.splitWidth,
            ),
            loc.x
        )
    }.render(UpdateEvent.EMPTY)

    override fun render(player: HudPlayer): (Long) -> PixelComponent = renderer(player)
}