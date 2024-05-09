package kr.toxicity.hud.compass

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.configuration.HudObjectType
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.*

class CircleCompass(
    resource: GlobalResource,
    assets: File,
    override val path: String,
    private val internalName: String,
    section: ConfigurationSection
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
    private val resourceRef = WeakReference(resource)
    private val length = section.getInt("length", 20).coerceAtLeast(20).coerceAtMost(360)
    private val encode = internalName.encodeKey()
    private val key = createAdventureKey(encode)
    private var center = 0xC0000
    private val array = WeakReference(JsonArray())
    private val applyOpacity = section.getBoolean("apply-opacity")
    private val scale = section.getDouble("scale", 1.0).apply {
        if (this <= 0) throw RuntimeException("scale cannot be <= 0")
    }
    private val scaleEquation = section.getString("scale-equation")?.let {
        TEquation(it)
    } ?: TEquation.one
    private val colorEquation = section.getConfigurationSection("color-equation")?.let {
        ColorEquation(it)
    } ?: defaultColorEquation
    private val space = section.getInt("space", 2).coerceAtLeast(0)

    private val shader = HudShader(
        GuiLocation(section.getConfigurationSection("gui").ifNull("gui value not set.")),
        section.getInt("layer"),
        section.getBoolean("outline")
    )
    private val pixel = ImageLocation(section.getConfigurationSection("pixel").ifNull("pixel value not set."))
    private val images = CompassImage(assets, section.getConfigurationSection("file").ifNull("file value not set."))
    private val conditions = section.toConditions().build(UpdateEvent.EMPTY)
    private val isDefault = ConfigManagerImpl.defaultCompass.contains(internalName) || section.getBoolean("default")

    private fun getKey(imageName: String, scaleMultiplier: Double, color: TextColor, image: BufferedImage, y: Int): WidthComponent {
        val char = (center++).parseChar()
        val nameEncoded = imageName.encodeKey()
        val maxHeight = (image.height.toDouble() * scale).roundToInt()
        val newHeight = (image.height.toDouble() * scale * scaleMultiplier).roundToInt()
        val div = newHeight.toDouble() / image.height.toDouble()
        array.get()?.add(JsonObject().apply {
            addProperty("type", "bitmap")
            addProperty("file", "$NAME_SPACE_ENCODED:${nameEncoded.encodeFolder()}/$nameEncoded.png")
            addProperty("ascent", HudImpl.createBit(pixel.y + y + (maxHeight - newHeight) / 2, shader))
            addProperty("height", newHeight)
            add("chars", JsonArray().apply {
                add(char)
            })
        })
        resourceRef.get()?.let {
            PackGenerator.addTask(ArrayList(it.textures).apply {
                add(nameEncoded.encodeFolder())
                add("$nameEncoded.png")
            }) {
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

    private inner class CompassImage(assets: File, section: ConfigurationSection) {
        val n = section.getConfigurationSection("n")?.let {
            CompassImageMap(assets, "n", it)
        }
        val e =section.getConfigurationSection("e")?.let {
            CompassImageMap(assets, "e", it)
        }
        val s = section.getConfigurationSection("s")?.let {
            CompassImageMap(assets, "s", it)
        }
        val w = section.getConfigurationSection("w")?.let {
            CompassImageMap(assets, "w", it)
        }
        val nw =section.getConfigurationSection("nw")?.let {
            CompassImageMap(assets, "nw", it)
        }
        val ne = section.getConfigurationSection("ne")?.let {
            CompassImageMap(assets, "ne", it)
        }
        val sw = section.getConfigurationSection("sw")?.let {
            CompassImageMap(assets, "sw", it)
        }
        val se = section.getConfigurationSection("se")?.let {
            CompassImageMap(assets, "se", it)
        }
        val chain = section.getConfigurationSection("chain")?.let {
            CompassImageMap(assets, "chain", it)
        }
        val point = section.getConfigurationSection("point")?.let {
            CompassImageMap(assets, "point", it)
        }
    }
    private class ColorEquation(
        val r: TEquation,
        val g: TEquation,
        val b: TEquation
    ) {
        constructor(section: ConfigurationSection): this(
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
        section: ConfigurationSection
    ) {
        val map = run {
            val fileName = section.getString("name").ifNull("name value not set.").replace('/', File.separatorChar)
            val scale = section.getDouble("scale", 1.0).apply {
                if (this <= 0.0) throw RuntimeException("scale cannot be <= 0.0")
            }
            val location = ImageLocation(section)
            val opacity = section.getDouble("opacity", 1.0).apply {
                if (this <= 0.0) throw RuntimeException("opacity cannot be <= 0.0")
            }
            val image = File(assets, fileName)
                .ifNotExist("this image doesn't exist: $fileName")
                .toImage()
                .removeEmptySide()
                .ifNull("invalid image: $fileName")
                .image
            val div = ceil(length.toDouble() / 2).toInt()
            if (applyOpacity) HashMap<CompassData, WidthComponent>().apply {
                for (i in 0..<div) {
                    val reverse = div - i
                    put(CompassData(reverse), getKey(
                        "compass_image_${internalName}_${imageName}_${i + 1}",
                        scaleEquation.evaluate(i.toDouble()).apply {
                            if (this < 0) throw RuntimeException("scale equation returns < 0")
                        },
                        colorEquation.evaluate(i.toDouble()),
                        image.withOpacity(sin(reverse.toDouble() / div.toDouble() * PI / 2) * opacity),
                        location.y,
                    ))
                }
            } else HashMap<CompassData, WidthComponent>().apply {
                for (i in 0..<div) {
                    val reverse = div - i
                    put(CompassData(reverse), location.x.toSpaceComponent() + getKey(
                        "compass_image_${internalName}_${imageName}_${i + 1}",
                        scaleEquation.evaluate(i.toDouble()).apply {
                            if (this <= 0.0) throw RuntimeException("scale equation returns <= 0")
                        } * scale,
                        colorEquation.evaluate(i.toDouble()),
                        image.withOpacity(opacity),
                        location.y
                    ))
                }
            }
        }
    }

    data class CompassData(val opacity: Int)

    init {
        array.get()?.let {
            PackGenerator.addTask(ArrayList(resource.font).apply {
                add(encode.encodeFolder())
                add("$encode.json")
            }) {
                JsonObject().apply {
                    add("providers", it)
                }.toByteArray()
            }
        }
    }
    override fun indicate(hudPlayer: HudPlayer): WidthComponent {
        if (!conditions(hudPlayer)) return EMPTY_WIDTH_COMPONENT
        val yaw =  hudPlayer.bukkitPlayer.location.yaw.toDouble()
        var degree = yaw
        if (degree < 0) degree += 360.0
        val div = (degree / 90 * length).roundToInt()
        var comp = EMPTY_WIDTH_COMPONENT
        val lengthDiv = length / 2
        val mod = (degree.toInt() % length).toDouble() / length
        val loc = hudPlayer.bukkitPlayer.location
        val world = hudPlayer.bukkitPlayer.world
        for (i in 1..<length) {
            comp += when (div - i + lengthDiv) {
                (length * 0.5).roundToInt() -> images.se
                length * 1 -> images.w
                (length * 1.5).roundToInt() -> images.nw
                length * 2 -> images.n
                (length * 2.5).roundToInt() -> images.ne
                length * 3 -> images.e
                (length * 3.5).roundToInt() -> images.sw
                0, length * 4 -> images.s
                else -> images.chain
            }?.map?.get(CompassData(if (i > lengthDiv) length - i else i))?.let {
                val glyphWidth = ((it.width + space) * (mod - 0.5)).roundToInt()
                (glyphWidth + space).toSpaceComponent() + it + (-glyphWidth + space).toSpaceComponent()
            } ?: (space * 2).toSpaceComponent()
        }
        images.point?.let { p ->
            hudPlayer.pointedLocation.forEach {
                val targetLoc = it.location
                if (targetLoc.world?.uid != world.uid) return@forEach
                var get = atan2(targetLoc.z - loc.z, targetLoc.x - loc.x)
                if (get < 0) get += 2 * PI
                var yawRadian = Math.toRadians(yaw + 90)
                if (yawRadian < 0) yawRadian += 2 * PI

                val minus = if (get > yawRadian) get - yawRadian else -(yawRadian - get)

                p.map[CompassData(ceil((length - abs(minus * length)) / 2).toInt())]?.let { pointComponent ->
                    val value = (minus * comp.width / 2 - comp.width / 2).roundToInt()
                    comp += (value - pointComponent.width / 2).toSpaceComponent() + pointComponent + (-value).toSpaceComponent()
                }
            }
        }
        return (-comp.width / 2).toSpaceComponent() + comp
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