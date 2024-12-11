package kr.toxicity.hud.compass.type

import com.google.gson.JsonArray
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.compass.CompassImpl
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.RenderScale
import kr.toxicity.hud.shader.ShaderProperty
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
) : CompassImpl, PlaceholderSource by PlaceholderSource.Impl(section) {
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
    private val length = section.getAsInt("length", 10).coerceAtLeast(20).coerceAtMost(360)
    private val encode = "compass_$internalName".encodeKey()
    private val key = createAdventureKey(encode)
    private var center = 0xC0000
    private val applyOpacity = section.getAsBoolean("apply-opacity", false)
    private val scale = section.getAsDouble("scale", 1.0).apply {
        if (this <= 0) throw RuntimeException("scale cannot be <= 0")
    }
    private val scaleEquation = section["scale-equation"]?.asString()?.let {
        TEquation(it)
    } ?: TEquation.one
    private val colorEquation = section["color-equation"]?.asObject()?.let {
        ColorEquation(it)
    } ?: defaultColorEquation
    private val space = section.getAsInt("space", 2).coerceAtLeast(0)

    private val pixel = PixelLocation(section["pixel"]?.asObject().ifNull("pixel value not set.")) + PixelLocation.hotBarHeight
    private val shader = HudShader(
        GuiLocation(section["gui"]?.asObject().ifNull("gui value not set.")),
        RenderScale.fromConfig(pixel, section),
        section.getAsInt("layer", 0),
        section.getAsBoolean("outline", false),
        pixel.opacity,
        ShaderProperty.properties(section["properties"]?.asArray())
    )
    private var array: JsonArray? = JsonArray()
    private val images = CompassImage(assets, section["file"]?.asObject().ifNull("file value not set."))
    private val conditions = section.toConditions(this) build UpdateEvent.EMPTY
    private val isDefault = ConfigManagerImpl.defaultCompass.contains(internalName) || section.getAsBoolean("default", false)


    private inner class CompassComponent(
        val x: Int,
        val char: String,
        val color: TextColor?,
        val width: Int
    ) {
        fun toWidthComponent(): WidthComponent {
            val comp = WidthComponent(Component.text()
                .content(char)
                .color(color)
                .font(key)
                .append(NEGATIVE_ONE_SPACE_COMPONENT.component), width)
            return if (x == 0) comp else x.toSpaceComponent() + comp
        }
    }

    private fun getKey(imageName: String, scaleMultiplier: Double, color: TextColor, image: BufferedImage, x: Int, y: Int): CompassComponent {
        val char = center++.parseChar()
        val nameEncoded = imageName.encodeKey()
        val maxHeight = (image.height.toDouble() * scale).roundToInt()
        val newHeight = (image.height.toDouble() * scale * scaleMultiplier).roundToInt()
        val div = newHeight.toDouble() / image.height.toDouble()
        array?.let { array ->
            createAscent(shader, pixel.y + y + (maxHeight - newHeight) / 2) { bit ->
                array += jsonObjectOf(
                    "type" to "bitmap",
                    "file" to "$NAME_SPACE_ENCODED:$nameEncoded.png",
                    "ascent" to bit,
                    "height" to newHeight,
                    "chars" to jsonArrayOf(char)
                )
            }
        }
        resourceRef?.let {
            PackGenerator.addTask(it.textures + "$nameEncoded.png") {
                image.toByteArray()
            }
        }
        return CompassComponent(x, char, if (color.value() != NamedTextColor.WHITE.value()) color else null, (image.width.toDouble() * div).roundToInt())
    }

    override fun getType(): HudObjectType<*> = HudObjectType.COMPASS
    override fun isDefault(): Boolean = isDefault
    override fun getName(): String = internalName

    private inner class CompassImage(assets: File, section: YamlObject) {
        val n = section["n"]?.asObject()?.let {
            CompassImageMap(assets, "n", it)
        }
        val e = section["e"]?.asObject()?.let {
            CompassImageMap(assets, "e", it)
        }
        val s = section["s"]?.asObject()?.let {
            CompassImageMap(assets, "s", it)
        }
        val w = section["w"]?.asObject()?.let {
            CompassImageMap(assets, "w", it)
        }
        val nw = section["nw"]?.asObject()?.let {
            CompassImageMap(assets, "nw", it)
        }
        val ne = section["ne"]?.asObject()?.let {
            CompassImageMap(assets, "ne", it)
        }
        val sw = section["sw"]?.asObject()?.let {
            CompassImageMap(assets, "sw", it)
        }
        val se = section["se"]?.asObject()?.let {
            CompassImageMap(assets, "se", it)
        }
        val chain = section["chain"]?.asObject()?.let {
            CompassImageMap(assets, "chain", it)
        }
        val point = section["point"]?.asObject()?.let {
            CompassImageMap(assets, "point", it)
        }
        val customIcon = section["custom-icon"]?.asObject()?.associate {
            it.key to CompassImageMap(assets, "custom_icon_${it.key}", it.value.asObject())
        } ?: emptyMap()

        val max = max(
            listOf(
                n,
                e,
                s,
                w,
                nw,
                ne,
                sw,
                se,
                chain,
                point
            ).maxOfOrNull max@ {
                (it ?: return@max 0).max
            } ?: 0,
            customIcon.values.maxOfOrNull {
                it.max
            } ?: 0
        )
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
            val fileName = section["name"]?.asString().ifNull("name value not set.").replace('/', File.separatorChar)
            val scale = section.getAsDouble("scale", 1.0).apply {
                if (this <= 0.0) throw RuntimeException("scale cannot be <= 0.0")
            }
            val location = PixelLocation(section)
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
                        location.x,
                        location.y
                    )
                }
            } else (0..<div).associate { i ->
                CompassData(div - i) to getKey(
                    "compass_image_${internalName}_${imageName}_${i + 1}",
                    scaleEquation.evaluate(i.toDouble()).apply {
                        if (this <= 0.0) throw RuntimeException("scale equation returns <= 0")
                    } * scale,
                    colorEquation.evaluate(i.toDouble()),
                    image.withOpacity(opacity),
                    location.x,
                    location.y
                )
            }
        }
        val max = map.values.maxOfOrNull {
            it.width
        } ?: 0
    }

    @JvmInline
    value class CompassData(val opacity: Int)

    init {
        array?.let {
            if (!BOOTSTRAP.useLegacyFont()) {
                val max = images.max + 2 * space + length
                val center = CURRENT_CENTER_SPACE_CODEPOINT
                it += buildJsonObject {
                    addProperty("type", "space")
                    add("advances", buildJsonObject {
                        for (i in -max..max) {
                            addProperty((center + i).parseChar(), i)
                        }
                    })
                }
            }
            PackGenerator.addTask(resource.font + "$encode.json") {
                jsonObjectOf("providers" to it).toByteArray()
            }
        }
    }

    private interface CompassComponentBuilder {
        infix fun append(component: CompassComponent?)
        infix fun build(player: HudPlayer): WidthComponent
    }

    private fun builder(yaw: Double, mod: Double) = if (BOOTSTRAP.useLegacyFont()) LegacyComponentBuilder(yaw, mod) else CurrentComponentBuilder(yaw, mod)

    //<=1.18
    private inner class LegacyComponentBuilder(
        private val yaw: Double,
        private val mod: Double
    ) : CompassComponentBuilder {

        private var comp = (-(images.max / 2 + space)).toSpaceComponent()

        override fun append(component: CompassComponent?) {
            comp += component?.let {
                val build = it.toWidthComponent()
                val move = (mod * (space * 2 + build.width)).roundToInt()
                (space + move).toSpaceComponent() + build + (space - move).toSpaceComponent()
            } ?: (space * 2).toSpaceComponent()
        }

        override fun build(player: HudPlayer): WidthComponent {
            val loc = player.location()
            val world = player.world()
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
                    val build = pointComponent.toWidthComponent()
                    val value = (minus * comp.width / 2 - comp.width / 2).roundToInt()
                    val halfPoint = build.width.toDouble() / 2
                    comp += (value - floor(halfPoint).toInt()).toSpaceComponent() + build + (-value - ceil(halfPoint).toInt()).toSpaceComponent()
                }
            }
            return (-comp.width / 2).toSpaceComponent() + comp
        }
    }
    private inner class CurrentComponentBuilder(
        private val yaw: Double,
        private val mod: Double
    ) : CompassComponentBuilder {

        private var comp = (-(images.max / 2 + space)).toSpaceComponent()
        private var append = EMPTY_WIDTH_COMPONENT
        private fun Int.spaceChar() = (CURRENT_CENTER_SPACE_CODEPOINT + this).parseChar()

        override fun append(component: CompassComponent?) {
            append += component?.let {
                val move = (mod * (space * 2 + it.width)).roundToInt()
                val build = WidthComponent(
                    Component.text()
                        .content(buildString {
                            append((space + move).spaceChar())
                            append(it.char)
                            append((space - move - 1).spaceChar())
                        })
                        .color(it.color),
                    space * 2 + it.width
                )
                if (it.x != 0) it.x.toSpaceComponent() + build else build
            } ?: (space * 2).toSpaceComponent()
        }

        override fun build(player: HudPlayer): WidthComponent {
            val loc = player.location()
            val world = player.world()
            append.component.font(key)
            comp += append
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
                    val build = pointComponent.toWidthComponent()
                    val value = (minus * comp.width / 2 - comp.width / 2).roundToInt()
                    val halfPoint = build.width.toDouble() / 2
                    comp += (value - floor(halfPoint).toInt()).toSpaceComponent() + build + (-value - ceil(halfPoint).toInt()).toSpaceComponent()
                }
            }
            return (-comp.width / 2).toSpaceComponent() + comp
        }
    }



    override fun indicate(player: HudPlayer): WidthComponent {
        if (!conditions(player)) return EMPTY_WIDTH_COMPONENT
        val yaw =  player.location().yaw.toDouble()
        var degree = yaw
        if (degree < 0) degree += 360.0
        val quarterDegree = degree / 90 * length
        val div = ceil(quarterDegree).toInt()
        val lengthDiv = length / 2
        val mod = quarterDegree - div + 0.5

        val builder = builder(yaw, mod)

        fun getKey(index: Int) = when (div - index + lengthDiv) {
            (length * 0.5).roundToInt() -> images.sw
            length * 1 -> images.w
            (length * 1.5).roundToInt() -> images.nw
            length * 2 -> images.n
            (length * 2.5).roundToInt() -> images.ne
            length * 3 -> images.e
            (length * 3.5).roundToInt() -> images.se
            0, length * 4 -> images.s
            else -> images.chain
        }?.map?.get(CompassData(if (index > lengthDiv) length - index else index))
        //var comp = (-((getKey(length / 2)?.width ?: 0) + space)).toSpaceComponent()
        for (i in 1..<length) {
            builder append getKey(i)
        }
        return builder build player
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