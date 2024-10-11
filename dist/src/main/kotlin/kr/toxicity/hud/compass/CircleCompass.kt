package kr.toxicity.hud.compass

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.RenderScale
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.*

class CircleCompass(
    resource: GlobalResource,
    assets: File,
    override val path: String,
    private val internalName: String,
    section: YamlObject
): CompassImpl {
    companion object {
        private val defaultColorEquation = TEquation("255").run {
            ColorEquation(
                this,
                this,
                this
            )
        }
    }
    private var resourceRef: GlobalResource? = resource
    private val length = section.getAsInt("length", 20).coerceAtLeast(20).coerceAtMost(360)
    private val encode = internalName.encodeKey()
    private val key = createAdventureKey(encode)
    private var center = 0xC0000
    private var array: JsonArray? = JsonArray()
    private val applyOpacity = section.getAsBoolean("apply-opacity", false)
    private val scale = section.getAsDouble("scale", 1.0).apply {
        if (this <= 0) throw RuntimeException("scale cannot be <= 0")
    }
    private val scaleEquation = section.get("scale-equation")?.asString()?.let {
        TEquation(it)
    } ?: TEquation.one
    private val colorEquation = section.get("color-equation")?.asObject()?.let {
        ColorEquation(it)
    } ?: defaultColorEquation
    private val space = section.getAsInt("space", 2).coerceAtLeast(0)

    private val pixel = ImageLocation(section.get("pixel")?.asObject().ifNull("pixel value not set.")) + ImageLocation.hotBarHeight
    private val shader = HudShader(
        GuiLocation(section.get("gui")?.asObject().ifNull("gui value not set.")),
        RenderScale.fromConfig(pixel, section),
        section.getAsInt("layer", 0),
        section.getAsBoolean("outline", false)
    )
    private val images = CompassImage(assets, section.get("file")?.asObject().ifNull("file value not set."))
    private val conditions = section.toConditions().build(UpdateEvent.EMPTY)
    private val isDefault = ConfigManagerImpl.defaultCompass.contains(internalName) || section.getAsBoolean("default", false)

    private fun getKey(imageName: String, scaleMultiplier: Double, color: TextColor, image: BufferedImage, y: Int): WidthComponent {
        val char = center++.parseChar()
        val nameEncoded = imageName.encodeKey()
        val maxHeight = (image.height.toDouble() * scale).roundToInt()
        val newHeight = (image.height.toDouble() * scale * scaleMultiplier).roundToInt()
        val div = newHeight.toDouble() / image.height.toDouble()
        array?.let { array ->
            HudImpl.createBit(shader, pixel.y + y + (maxHeight - newHeight) / 2) { bit ->
                array.add(jsonObjectOf(
                    "type" to "bitmap",
                    "file" to "$NAME_SPACE_ENCODED:$nameEncoded.png",
                    "ascent" to bit,
                    "height" to newHeight,
                    "chars" to jsonArrayOf(char)
                ))
            }
        }
        resourceRef?.let {
            PackGenerator.addTask(it.textures + "$nameEncoded.png") {
                image.toByteArray()
            }
        }
        return WidthComponent(Component.text()
            .content(char)
            .color(color)
            .font(key)
            .append(NEGATIVE_ONE_SPACE_COMPONENT.component), (image.width.toDouble() * div).roundToInt())
    }

    override fun getType(): HudObjectType<*> = HudObjectType.COMPASS
    override fun isDefault(): Boolean = isDefault
    override fun getName(): String = internalName

    private inner class CompassImage(assets: File, section: YamlObject) {
        val n = section.get("n")?.asObject()?.let {
            CompassImageMap(assets, "n", it)
        }
        val e = section.get("e")?.asObject()?.let {
            CompassImageMap(assets, "e", it)
        }
        val s = section.get("s")?.asObject()?.let {
            CompassImageMap(assets, "s", it)
        }
        val w = section.get("w")?.asObject()?.let {
            CompassImageMap(assets, "w", it)
        }
        val nw = section.get("nw")?.asObject()?.let {
            CompassImageMap(assets, "nw", it)
        }
        val ne = section.get("ne")?.asObject()?.let {
            CompassImageMap(assets, "ne", it)
        }
        val sw = section.get("sw")?.asObject()?.let {
            CompassImageMap(assets, "sw", it)
        }
        val se = section.get("se")?.asObject()?.let {
            CompassImageMap(assets, "se", it)
        }
        val chain = section.get("chain")?.asObject()?.let {
            CompassImageMap(assets, "chain", it)
        }
        val point = section.get("point")?.asObject()?.let {
            CompassImageMap(assets, "point", it)
        }
        val customIcon = section.get("custom-icon")?.asObject()?.associate {
            it.key to CompassImageMap(assets, "custom_icon_${it.key}", it.value.asObject())
        } ?: emptyMap()
    }
    private class ColorEquation(
        val r: TEquation,
        val g: TEquation,
        val b: TEquation
    ) {
        constructor(section: YamlObject): this(
            section.getTEquation("r").ifNull("r value not set."),
            section.getTEquation("g").ifNull("g value not set."),
            section.getTEquation("b").ifNull("b value not set.")
        )

        fun evaluate(t: Double): TextColor {
            fun get(equation: TEquation) = equation.evaluate(t).apply {
                if (this < 0) throw RuntimeException("color equation returns < 0")
                if (this > 0xFF) throw RuntimeException("color equation returns > 0xFF")
            }.toInt()
            return TextColor.color(get(r), get(g), get(b))
        }
    }

    private inner class CompassImageMap(
        assets: File,
        imageName: String,
        section: YamlObject
    ) {
        val map = run {
            val fileName = section.get("name")?.asString().ifNull("name value not set.").replace('/', File.separatorChar)
            val scale = section.getAsDouble("scale", 1.0).apply {
                if (this <= 0.0) throw RuntimeException("scale cannot be <= 0.0")
            }
            val location = ImageLocation(section)
            val opacity = section.getAsDouble("opacity", 1.0).apply {
                if (this <= 0.0) throw RuntimeException("opacity cannot be <= 0.0")
            }
            val image = File(assets, fileName)
                .ifNotExist("this image doesn't exist: $fileName")
                .toImage()
                .removeEmptySide()
                .ifNull("invalid image: $fileName")
                .image
            val div = ceil(length.toDouble() / 2).toInt()
            if (applyOpacity) {
                (0..<div).associate { i ->
                    val reverse = div - i
                    CompassData(reverse) to getKey(
                        "compass_image_${internalName}_${imageName}_${i + 1}",
                        scaleEquation.evaluate(i.toDouble()).apply {
                            if (this < 0) throw RuntimeException("scale equation returns < 0")
                        },
                        colorEquation.evaluate(i.toDouble()),
                        image.withOpacity(sin(reverse.toDouble() / div.toDouble() * PI / 2) * opacity),
                        location.y,
                    )
                }
            } else (0..<div).associate { i ->
                CompassData(div - i) to location.x.toSpaceComponent() + getKey(
                    "compass_image_${internalName}_${imageName}_${i + 1}",
                    scaleEquation.evaluate(i.toDouble()).apply {
                        if (this <= 0.0) throw RuntimeException("scale equation returns <= 0")
                    } * scale,
                    colorEquation.evaluate(i.toDouble()),
                    image.withOpacity(opacity),
                    location.y
                )
            }
        }
    }

    data class CompassData(val opacity: Int)

    init {
        array?.let {
            PackGenerator.addTask(resource.font + "$encode.json") {
                jsonObjectOf("providers" to it).toByteArray()
            }
        }
    }

    override fun indicate(player: HudPlayer): WidthComponent {
        if (!conditions(player)) return EMPTY_WIDTH_COMPONENT
        val yaw =  player.location().yaw.toDouble()
        var degree = yaw
        if (degree < 0) degree += 360.0
        val div = (degree / 90 * length).roundToInt()
        var comp = EMPTY_WIDTH_COMPONENT
        val lengthDiv = length / 2
        val mod = (degree.toInt() % length).toDouble() / length
        val loc = player.location()
        val world = player.world()
        for (i in 1..<length) {
            comp += when (div - i + lengthDiv) {
                (length * 0.5).roundToInt() -> images.sw
                length * 1 -> images.w
                (length * 1.5).roundToInt() -> images.nw
                length * 2 -> images.n
                (length * 2.5).roundToInt() -> images.ne
                length * 3 -> images.e
                (length * 3.5).roundToInt() -> images.se
                0, length * 4 -> images.s
                else -> images.chain
            }?.map?.get(CompassData(if (i > lengthDiv) length - i else i))?.let {
                val glyphWidth = ((it.width + space) * (mod - 0.5)).roundToInt()
                (glyphWidth + space).toSpaceComponent() + it + (-glyphWidth + space).toSpaceComponent()
            } ?: (space * 2).toSpaceComponent()
        }
        player.pointedLocation.forEach {
            val selectedPointer = it.icon?.let { s -> images.customIcon[s] } ?: images.point ?: return@forEach

            val targetLoc = it.location
            if (targetLoc.world.uuid != world.uuid) return@forEach
            var get = atan2(targetLoc.z - loc.z, targetLoc.x - loc.x) / PI
            if (get < 0) get += 2
            var yawCal = (if (yaw > 90) -270 + yaw else 90 + yaw) / 180
            if (yawCal < 0) yawCal += 2

            val min = absMin(get - yawCal, -(yawCal - get))
            val minus = absMin(if (min > 0) -(2 - min) else 2 + min, min)

            selectedPointer.map[CompassData(ceil((length - abs(minus * length)) / 2).toInt())]?.let { pointComponent ->
                val value = (minus * comp.width / 2 - comp.width / 2).roundToInt()
                comp += (value - pointComponent.width / 2).toSpaceComponent() + pointComponent + (-value).toSpaceComponent()
            }
        }
        return (-comp.width / 2).toSpaceComponent() + comp
    }

    private fun absMin(d1: Double, d2: Double): Double {
        return if (abs(d1) < abs(d2)) d1 else d2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CircleCompass

        return internalName == other.internalName
    }

    override fun hashCode(): Int {
        return internalName.hashCode()
    }


}