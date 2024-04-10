package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LocatedImage
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudText
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStreamReader
import kotlin.math.roundToInt

object TextManager: BetterHudManager {

    private const val CHAR_LENGTH = 16

    private val antialiasing = RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    private val fractionalMetrics = RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    private val textMap = HashMap<String, HudText>()

    private val textWidthMap = HashMap<Char, Int>()
    private val textKeyMap = mutableMapOf<ShaderGroup, HudTextData>()

    private val defaultBitmapImageMap = HashMap<Char, BufferedImage>()

    fun getKey(shaderGroup: ShaderGroup) = textKeyMap[shaderGroup]
    fun setKey(shaderGroup: ShaderGroup, key: HudTextData) {
        textKeyMap[shaderGroup] = key
    }

    override fun start() {
        loadDefaultBitmap()
    }
    fun getWidth(char: Char) = textWidthMap[char] ?: 3

    fun getText(name: String) = textMap[name]

    override fun reload(resource: GlobalResource) {
        textMap.clear()
        textWidthMap.clear()
        textKeyMap.clear()
        val assetsFolder = DATA_FOLDER.subFolder("assets")
        val fontFolder = DATA_FOLDER.subFolder("fonts")
        val globalSaveFolder = resource.textures.subFolder("text")
        DATA_FOLDER.subFolder("texts").forEachAllYaml { file, s, section ->
            runCatching {
                val fontDir = section.getString("file")
                val scale = section.getInt("scale")
                val fontTarget = fontDir?.let {
                    File(fontFolder, it).ifNotExist("this file doesn't exist: $it")
                }
                val fontFile = (fontTarget?.inputStream()?.buffered()?.use {
                    Font.createFont(Font.TRUETYPE_FONT, it)
                } ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().font).deriveFont(scale.toFloat())
                val saveName = "${fontTarget?.nameWithoutExtension ?: s}_$scale"
                textMap[s] = parseFont(s, saveName, fontFile, scale, globalSaveFolder, HashMap<String, LocatedImage>().apply {
                    section.getConfigurationSection("images")?.forEachSubConfiguration { key, configurationSection ->
                        put(key, LocatedImage(
                            File(assetsFolder, configurationSection.getString("name").ifNull("image does not set: $key"))
                                .ifNotExist("this image doesn't exist: $key")
                                .toImage()
                                .removeEmptyWidth()
                                .ifNull("invalid image: $key"),
                            ImageLocation(configurationSection),
                            configurationSection.getDouble("scale", 1.0).apply {
                                if (this <= 0.0) throw RuntimeException("scale cannot be <= 0: $key")
                            }
                        ))
                    }
                }, section.toConditions(), section.getBoolean("merge-default-bitmap"))
            }.onFailure { e ->
                warn("Unable to load this text: $s in ${file.name}")
                warn("Reason: ${e.message}")
            }
        }
        val defaultArray = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("type", "space")
                add("advances", JsonObject().apply {
                    addProperty(" ", 4)
                })
            })
        }
        val fontConfig = File(DATA_FOLDER, "font.yml").apply {
            if (!exists()) PLUGIN.saveResource("font.yml", false)
        }.toYaml()
        val configScale = fontConfig.getInt("scale", 16)
        val configHeight = fontConfig.getInt("height", 9)
        val configAscent = fontConfig.getInt("ascent", 8).coerceAtMost(configHeight)
        val defaultFont = File(DATA_FOLDER, ConfigManager.defaultFontName).run {
            (if (exists()) runCatching {
                inputStream().buffered().use {
                    Font.createFont(Font.TRUETYPE_FONT, it)
                }
            }.getOrNull() else null) ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().font
        }.deriveFont(configScale.toFloat())
        val parseDefault = parseFont("default", "default", defaultFont, configScale, resource.textures.subFolder("font"), emptyMap(),  ConditionBuilder.alwaysTrue, fontConfig.getBoolean("merge-default-bitmap", true))
        val heightMultiply = configHeight.toDouble() / parseDefault.height.toDouble()
        parseDefault.charWidth.forEach {
            textWidthMap[it.key] = Math.round(it.value.toDouble() * heightMultiply).toInt()
        }
        parseDefault.array.forEach {
            defaultArray.add(JsonObject().apply {
                addProperty("type", "bitmap")
                addProperty("file", "$NAME_SPACE:font/default/${it.file}")
                addProperty("ascent", configAscent)
                addProperty("height", configHeight)
                add("chars", it.chars)
            })
        }
        JsonObject().apply {
            add("providers", defaultArray)
        }.save(resource.font.subFile("default.json"))
    }

    private fun loadDefaultBitmap() {
        defaultBitmapImageMap.clear()
        PLUGIN.getResource("default.json")?.let {
            runCatching {
                InputStreamReader(it).buffered().use { reader ->
                    JsonParser.parseReader(reader)
                }.asJsonObject.getAsJsonArray("providers").forEachIndexed { debugIndex, element ->
                    val obj = element.asJsonObject
                    val imageName = (obj.getAsJsonPrimitive("file") ?: run {
                        warn("Unable to find file name in this sector: $debugIndex")
                        return
                    }).asString.run {
                        if (contains('/')) substringAfterLast('/') else this
                    }
                    val image = runCatching {
                        PLUGIN.getResource(imageName)?.buffered()?.toImage() ?: run {
                            warn("Unable to find this resource: $imageName")
                            return
                        }
                    }.getOrElse { e ->
                        warn("Unable to load this image: $imageName")
                        warn("Reason: ${e.message}")
                        return
                    }
                    val array = obj.getAsJsonArray("chars")
                    val height = image.height / array.size()
                    array.forEachIndexed { i1, charElement ->
                        val str = charElement.asString
                        val width = image.width / str.length
                        str.forEachIndexed { i2, char ->
                            defaultBitmapImageMap[char] = image.getSubimage(width * i2, height * i1, width, height)
                        }
                    }
                }
            }.onFailure { e ->
                warn("Unable to parse default.json")
                warn("Reason: ${e.message}")
            }
        }
    }

    private fun parseFont(
        s: String,
        saveName: String,
        fontFile: Font,
        scale: Int,
        imageSaveFolder: File,
        images: Map<String, LocatedImage>,
        condition: ConditionBuilder,
        mergeDefaultBitmap: Boolean
    ): HudText {
        val height = (scale.toDouble() * 1.4).toInt()
        val pairMap = HashMap<Int, MutableList<Pair<Char, Image>>>()
        val charWidthMap = HashMap<Char, Int>()
        if (mergeDefaultBitmap) defaultBitmapImageMap.forEach {
            val newWidth = ((height.toDouble() / it.value.height) * it.value.width).roundToInt()
            BufferedImage(newWidth, height, BufferedImage.TYPE_INT_ARGB).apply {
                createGraphics().run {
                    drawImage(it.value.getScaledInstance(newWidth, height, BufferedImage.SCALE_SMOOTH), 0, 0, null)
                    dispose()
                }
            }.removeEmptyWidth()?.let { resizedImage ->
                pairMap.getOrPut(resizedImage.image.width) {
                    ArrayList()
                }.add(it.key to resizedImage.image)
                charWidthMap[it.key] = resizedImage.image.width
            }
        }
        (Char.MIN_VALUE..Char.MAX_VALUE).forEach { char ->
            if (fontFile.canDisplay(char) && !charWidthMap.containsKey(char)) {
                BufferedImage(scale, height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        font = fontFile
                        renderingHints.add(antialiasing)
                        renderingHints.add(fractionalMetrics)
                        drawString(char.toString(), 0, scale)
                        dispose()
                    }
                }.removeEmptyWidth()?.let {
                    pairMap.getOrPut(it.image.width) {
                        ArrayList()
                    }.add(char to it.image)
                    charWidthMap[char] = it.image.width
                }
            }
        }
        val textList = ArrayList<HudTextArray>()
        val saveFolder = imageSaveFolder.subFolder(saveName)
        var i = 0
        images.forEach {
            it.value.image.image.save(File(saveFolder, "image_${it.key}.png"))
        }
        pairMap.forEach {
            val width = it.key
            fun save(list: List<Pair<Char, Image>>) {
                val name = "${saveName}_${++i}.png"
                val json = JsonArray()
                BufferedImage(width * list.size.coerceAtMost(CHAR_LENGTH), height * (((list.size - 1) / CHAR_LENGTH) + 1), BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                        renderingHints.add(antialiasing)
                        renderingHints.add(fractionalMetrics)
                        list.forEachIndexed { index, pair ->
                            drawImage(pair.second, width * (index % CHAR_LENGTH), height * (index / CHAR_LENGTH), null)
                        }
                        list.split(CHAR_LENGTH).forEach { subList ->
                            json.add(subList.map { pair ->
                                pair.first
                            }.joinToString(""))
                        }
                        dispose()
                    }
                }.save(File(saveFolder, name))
                textList.add(HudTextArray(name, json))
            }
            it.value.split(CHAR_LENGTH * CHAR_LENGTH).forEach { target ->
                if (target.size % CHAR_LENGTH == 0 || target.size < CHAR_LENGTH) {
                    save(target)
                } else {
                    val split = target.split(CHAR_LENGTH)
                    save(split.subList(0, split.lastIndex).sum())
                    save(split.last())
                }
            }
        }
        return HudText(s, saveName, height, textList, images, charWidthMap, condition)
    }

    override fun end() {
    }
}