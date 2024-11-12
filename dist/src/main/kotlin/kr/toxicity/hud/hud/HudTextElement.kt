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
import kr.toxicity.hud.manager.TextManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.renderer.TextRenderer
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import kotlin.math.roundToInt

class HudTextElement(
    parent: HudImpl,
    resource: GlobalResource,
    private val text: TextLayout,
    gui: GuiLocation,
    pixel: PixelLocation
) {

    private val renderer = run {
        val loc = text.location + pixel
        val shader = HudShader(
            gui,
            text.renderScale,
            text.layer,
            text.outline,
            loc.opacity,
            text.property
        )
        val imageCodepointMap = text.imageCharMap.map {
            it.value.name to it.key
        }.toMap()
        val index2 = ++parent.textIndex
        val keys = (0..<text.line).map { lineIndex ->
            val yAxis = (loc.y + lineIndex * text.lineWidth).coerceAtLeast(-HudImpl.ADD_HEIGHT).coerceAtMost(HudImpl.ADD_HEIGHT)
            val group = ShaderGroup(shader, text.text.name, text.scale, yAxis)
            TextManagerImpl.getKey(group) ?: run {
                val array = text.startJson()
                text.text.array.forEach {
                    HudImpl.createBit(shader, yAxis) { y ->
                        array.add(jsonObjectOf(
                            "type" to "bitmap",
                            "file" to "$NAME_SPACE_ENCODED:${it.file}",
                            "ascent" to y,
                            "height" to (it.height * text.scale).roundToInt(),
                            "chars" to it.chars
                        ))
                    }
                }
                var textIndex = TEXT_IMAGE_START_CODEPOINT + text.imageCharMap.size
                val textEncoded = "hud_${parent.name}_text_${index2 + 1}_${lineIndex + 1}".encodeKey()
                val key = createAdventureKey(textEncoded)
                text.imageCharMap.forEach {
                    val height = (it.value.height.toDouble() * text.scale * text.emojiScale).roundToInt()
                    HudImpl.createBit(shader, loc.y + it.value.location.y + lineIndex * text.lineWidth) { y ->
                        array.add(
                            jsonObjectOf(
                                "type" to "bitmap",
                                "file" to it.value.fileName,
                                "ascent" to y,
                                "height" to height,
                                "chars" to jsonArrayOf(it.key.parseChar())
                            )
                        )
                    }
                }
                PackGenerator.addTask(resource.font + "$textEncoded.json") {
                    jsonObjectOf("providers" to array).toByteArray()
                }
                BackgroundKey(
                    key,
                    //TODO replace it to proper background in the future.
                    text.background?.let {
                        fun getString(image: LoadedImage, file: String): WidthComponent {
                            val result = (++textIndex).parseChar()
                            val height = (image.image.height.toDouble() * text.backgroundScale).roundToInt()
                            val div = height.toDouble() / image.image.height
                            HudImpl.createBit(HudShader(
                                gui,
                                text.renderScale,
                                text.layer - 1,
                                false,
                                loc.opacity * it.location.opacity,
                                text.property
                            ), loc.y + it.location.y + lineIndex * text.lineWidth) { y ->
                                array.add(jsonObjectOf(
                                    "type" to "bitmap",
                                    "file" to "$NAME_SPACE_ENCODED:$file.png",
                                    "ascent" to y,
                                    "height" to height,
                                    "chars" to jsonArrayOf(result)
                                ))
                            }
                            return WidthComponent(Component.text()
                                .font(key)
                                .content(result)
                                .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.image.width.toDouble() * div).roundToInt())
                        }
                        BackgroundLayout(
                            it.location.x,
                            getString(it.left, "background_${it.name}_left".encodeKey()),
                            getString(it.right, "background_${it.name}_right".encodeKey()),
                            getString(it.body, "background_${it.name}_body".encodeKey())
                        )
                    }
                ).apply {
                    TextManagerImpl.setKey(group, this)
                }
            }
        }
        TextRenderer(
            text.text.charWidth,
            text.imageCharMap,
            text.color,
            HudTextData(
                keys,
                imageCodepointMap,
                text.splitWidth,
            ),
            text.pattern,
            text.align,
            text.lineAlign,
            text.scale,
            text.emojiScale,
            loc.x,
            text.numberEquation,
            text.numberFormat,
            text.disableNumberFormat,
            text.follow,
            text.cancelIfFollowerNotExists,
            text.useLegacyFormat,
            text.legacySerializer,
            text.space,
            text.conditions and text.text.conditions
        )
    }.getText(UpdateEvent.EMPTY)

    fun getText(hudPlayer: HudPlayer): PixelComponent = renderer(hudPlayer)
}