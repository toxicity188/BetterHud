package kr.toxicity.hud.popup

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.hud.Hud
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.ListenerHudImage
import kr.toxicity.hud.image.LocationGroup
import kr.toxicity.hud.image.SplitType
import kr.toxicity.hud.layout.LayoutGroup
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
import kotlin.math.ceil
import kotlin.math.floor

class PopupLayout(
    private val layout: LayoutGroup,
    private val parent: PopupImpl,
    private val name: String,
    private val globalLocation: GuiLocation,
    file: File
) {
    private var imageChar = 0xCE000
    private var textIndex = 0

    companion object {
        private val textKeyMap = mutableMapOf<ShaderGroup, Key>()

        fun clear() = textKeyMap.clear()
    }

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

    fun getComponent(index: Int, animationIndex: Int, player: HudPlayer) = groups[index % groups.size].getComponent(animationIndex, player)
    fun getComponent(player: HudPlayer): (Int, Int) -> WidthComponent {
        val map = groups.map {
            it.getComponent(player)
        }
        return { index, frame ->
            val get = map[index % map.size]
            get[frame % get.size]
        }
    }

    private inner class PopupLayoutGroup(pair: LocationGroup, val array: JsonArray, textFolder: File) {
        val elements = layout.animation.map { location ->
            PopupElement(pair, array, location, textFolder)
        }
        fun getComponent(animationIndex: Int, player: HudPlayer) = elements[animationIndex % elements.size].getComponent(player)
        fun getComponent(player: HudPlayer) = elements.map {
            it.getComponent(player)
        }
    }
    private inner class PopupElement(pair: LocationGroup, val array: JsonArray, location: ImageLocation, textFolder: File) {
        private val gui = pair.gui + parent.gui + globalLocation

        fun getComponent(player: HudPlayer) = LayoutComponentContainer()
            .append(image.map {
                it.getComponent(player)
            })
            .append(texts.map {
                it.getText(player)
            })
            .build()

        val image = layout.image.map { target ->
            val hudImage = target.image
            val imageShader = HudShader(
                gui,
                target.layer,
                target.outline
            )
            val pixel = location + pair.pixel + target.location
            val maxWidth = ceil(hudImage.image.maxOf {
                it.second.width.toDouble()
            } * target.scale).toInt()
            ImageRenderer(
                hudImage,
                if (hudImage.image.size > 1) hudImage.image.map {
                    val char = (++imageChar).parseChar()
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE:image/${hudImage.name}/${it.first}")
                        addProperty("ascent", Hud.createBit(pixel.y, imageShader))
                        addProperty("height", ceil(it.second.height * target.scale).toInt())
                        add("chars", JsonArray().apply {
                            add(char)
                        })
                    })
                    var comp = WidthComponent(Component.text(char).font(imageKey), ceil(it.second.width.toDouble() * target.scale).toInt()) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER
                    if (hudImage is ListenerHudImage) {
                        when (hudImage.splitType) {
                            SplitType.RIGHT -> {
                                comp = (maxWidth - comp.width).toSpaceComponent() + comp
                            }
                            SplitType.UP, SplitType.DOWN -> {
                                val minus = (maxWidth - comp.width).toDouble()
                                comp = ceil(minus / 2).toInt().toSpaceComponent() + comp + floor(minus / 2).toInt().toSpaceComponent()
                            }
                            else -> {}
                        }
                    }
                    pixel.x.toSpaceComponent() + comp
                } else listOf(hudImage.image[0].let {
                    val char = (++imageChar).parseChar()
                    array.add(JsonObject().apply {
                        addProperty("type", "bitmap")
                        addProperty("file", "$NAME_SPACE:image/${it.first}")
                        addProperty("ascent", Hud.createBit(pixel.y, imageShader))
                        addProperty("height", ceil(it.second.height * target.scale).toInt())
                        add("chars", JsonArray().apply {
                            add(char)
                        })
                    })
                    val comp = WidthComponent(Component.text(char).font(imageKey), ceil(it.second.width.toDouble() * target.scale).toInt())
                    pixel.x.toSpaceComponent() + comp
                })
            ) {
                hudImage.conditions(it) && target.conditions(it)
            }
        }
        val texts = layout.text.map { textLayout ->
            val pixel = location + pair.pixel + textLayout.location
            val textShader = HudShader(
                gui,
                textLayout.layer,
                textLayout.outline
            )
            val group = ShaderGroup(textShader, textLayout.text.name, pixel.y)
            val textKey = textKeyMap[group] ?: run {
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
                        addProperty("height", ceil(textLayout.text.height.toDouble() * textLayout.scale).toInt())
                        add("chars", it.chars)
                    })
                }
                JsonObject().apply {
                    add("providers", array)
                }.save(File(textFolder, "text_${index}.json"))
                val key = Key.key("$NAME_SPACE:popup/${parent.name}/$name/text/text_${index}")
                textKeyMap[group] = key
                key
            }
            TextRenderer(
                textLayout.text.charWidth,
                Style.style(textLayout.color).font(textKey),
                textLayout.pattern,
                textLayout.align,
                textLayout.scale,
                pixel.x,
                textLayout.space,
            ) {
                textLayout.conditions(it) && textLayout.text.conditions(it)
            }
        }
    }
}