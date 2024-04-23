package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.image.LocatedImage
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.resource.KeyResource
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudText
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.HudTextData
import kr.toxicity.hud.util.*
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStreamReader
import kotlin.math.roundToInt

object TextManager: BetterHudManager {

    private const val CHAR_LENGTH = 16

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

    fun getText(name: String) = synchronized(textMap) {
        textMap[name]
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        synchronized(this) {
            textMap.clear()
            textWidthMap.clear()
            textKeyMap.clear()
        }
        val assetsFolder = DATA_FOLDER.subFolder("assets")
        val fontFolder = DATA_FOLDER.subFolder("fonts")
        DATA_FOLDER.subFolder("texts").forEachAllYamlAsync({ file, s, section ->
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
                textMap.putSync("text", s) {
                    parseFont(file.path, s, saveName, fontFile, scale, resource.textures, HashMap<String, LocatedImage>().apply {
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
                }
            }.onFailure { e ->
                warn(
                    "Unable to load this text: $s in ${file.name}",
                    "Reason: ${e.message}"
                )
            }
        }) {
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
            val parseDefault = parseFont("", "default", "default", defaultFont, configScale, resource.textures, emptyMap(),  ConditionBuilder.alwaysTrue, fontConfig.getBoolean("merge-default-bitmap", true))
            val heightMultiply = configHeight.toDouble() / parseDefault.height.toDouble()
            parseDefault.charWidth.forEach {
                textWidthMap[it.key] = Math.round(it.value.toDouble() * heightMultiply).toInt()
            }
            parseDefault.array.forEach {
                defaultArray.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "$NAME_SPACE_ENCODED:${it.file.substringBefore('.')}/${it.file}")
                    addProperty("ascent", configAscent)
                    addProperty("height", configHeight)
                    add("chars", it.chars)
                })
            }
            PackGenerator.addTask(ArrayList(resource.font).apply {
                add(KeyResource.default)
                add("${KeyResource.default}.json")
            }) {
                JsonObject().apply {
                    add("providers", defaultArray)
                }.toByteArray()
            }
            callback()
        }
    }

    private fun loadDefaultBitmap() {
        defaultBitmapImageMap.clear()
        PLUGIN.getResource("minecraft_default.json")?.let {
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
                        warn(
                            "Unable to load this image: $imageName",
                            "Reason: ${e.message}"
                        )
                        return
                    }
                    val array = obj.getAsJsonArray("chars")
                    val height = image.height / array.size()
                    array.forEachIndexed { i1, charElement ->
                        val str = charElement.asString
                        val width = image.width / str.length
                        var i2 = 0
                        str.codePoints().forEach { char ->
                            defaultBitmapImageMap[char.toChar()] = image.getSubimage(width * (i2++), height * i1, width, height)
                        }
                    }
                }
            }.onFailure { e ->
                warn(
                    "Unable to parse minecraft_default.json",
                    "Reason: ${e.message}"
                )
            }
        }
    }

    private fun parseFont(
        path: String,
        s: String,
        saveName: String,
        fontFile: Font,
        scale: Int,
        imageSaveFolder: List<String>,
        images: Map<String, LocatedImage>,
        condition: ConditionBuilder,
        mergeDefaultBitmap: Boolean
    ): HudText {
        val height = (scale.toDouble() * 1.4).toInt()
        val pairMap = HashMap<Int, MutableList<Pair<Char, Image>>>()
        val charWidthMap = HashMap<Char, Int>()
        if (mergeDefaultBitmap) defaultBitmapImageMap.entries.toList().forEachAsync {
            val newWidth = ((height.toDouble() / it.value.height) * it.value.width).roundToInt()
            BufferedImage(newWidth, height, BufferedImage.TYPE_INT_ARGB).apply {
                createGraphics().run {
                    drawImage(it.value.getScaledInstance(newWidth, height, BufferedImage.SCALE_SMOOTH), 0, 0, null)
                    dispose()
                }
            }.fontSubImage()?.let { resizedImage ->
                pairMap.getOrPut(resizedImage.width) {
                    ArrayList()
                }.add(it.key to resizedImage)
                charWidthMap[it.key] = resizedImage.width
            }
        }
        (Char.MIN_VALUE..Char.MAX_VALUE).filter { char ->
            fontFile.canDisplay(char) && !charWidthMap.containsKey(char)
        }.forEachAsync { char ->
            val image = BufferedImage(scale, height, BufferedImage.TYPE_INT_ARGB).processFont(char, fontFile) ?: return@forEachAsync
            pairMap.getOrPut(image.width) {
                ArrayList()
            }.add(char to image)
            charWidthMap[char] = image.width
        }
        val textList = ArrayList<HudTextArray>()
        var i = 0
        images.forEach {
            PackGenerator.addTask(ArrayList(imageSaveFolder).apply {
                val encode = "glyph_${it.key}".encodeKey()
                add(encode)
                add("$encode.png")
            }) {
                it.value.image.image.toByteArray()
            }
        }
        pairMap.forEach {
            val width = it.key
            fun save(list: List<Pair<Char, Image>>) {
                val encode = "text_${saveName}_${++i}".encodeKey()
                val name = "$encode.png"
                val json = JsonArray()
                list.split(CHAR_LENGTH).forEach { subList ->
                    json.add(subList.map { pair ->
                        pair.first
                    }.joinToString(""))
                }
                PackGenerator.addTask(ArrayList(imageSaveFolder).apply {
                    add(encode)
                    add(name)
                }) {
                    BufferedImage(width * list.size.coerceAtMost(CHAR_LENGTH), height * (((list.size - 1) / CHAR_LENGTH) + 1), BufferedImage.TYPE_INT_ARGB).apply {
                        createGraphics().run {
                            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                            list.forEachIndexed { index, pair ->
                                drawImage(pair.second, width * (index % CHAR_LENGTH), height * (index / CHAR_LENGTH), null)
                            }
                            dispose()
                        }
                    }.toByteArray()
                }
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
        return HudText(path, s, saveName, height, textList, images, charWidthMap, condition)
    }

    override fun end() {
    }
}