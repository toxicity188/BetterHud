package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kr.toxicity.hud.configuration.PluginConfiguration
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
import net.kyori.adventure.audience.Audience
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object TextManager: BetterHudManager {

    private const val CHAR_LENGTH = 16

    private val textMap = HashMap<String, HudText>()
    private val textCacheMap = HashMap<String, HudText>()

    private val textWidthMap = HashMap<Int, Int>()
    private val textKeyMap = ConcurrentHashMap<ShaderGroup, HudTextData>()

    private val defaultBitmapImageMap = HashMap<Int, BufferedImage>()
    private val translatableString = HashMap<String, Map<String, String>>()

    private val unicodeRange = (0..0x10FFFF).toList()

    private val defaultLatin = HashSet<Int>().apply {
        addAll(0x0021..0x0026)
        addAll(0x0028..0x002F)
        addAll(0x003A..0x0040)
        addAll(0x005B..0x0060)
        addAll(0x007B..0x007E)
        addAll(0x00A0..0x00BF)
        add(0x0027)
        addAll(0x0030..0x0039)
        addAll(0x0041..0x005A)
        addAll(0x0061..0x007A)
        addAll(0x00C0..0x00F6)
        addAll(0x00F8..0x00FF)
        addAll(0x0100..0x017F)
    }

    private val languageCodepointRange = mapOf(
        "korean" to HashSet<Int>().apply {
            addAll(0x1100..0x1112)
            addAll(0x1161..0x1175)
            addAll(0x11A8..0x11C2)
            addAll(0xAC00..0xD7A3)
        },
        "japan" to HashSet<Int>().apply {
            addAll(0x3041..0x3096)
            addAll(0x30A1..0x30FA)
        },
        "china" to HashSet<Int>().apply {
            addAll(0x4E00..0x9FFF)
        },
        "russia" to HashSet<Int>().apply {
            addAll(0x0400..0x045F)
        },
        "bengal" to HashSet<Int>().apply {
            addAll(0x0985..0x09B9)
        },
        "thailand" to HashSet<Int>().apply {
            addAll(0x0E01..0x0E3A)
            addAll(0x0E40..0x0E4E)
        },
        "greece" to HashSet<Int>().apply {
            addAll(0x0390..0x03CE)
        },
        "hindi" to HashSet<Int>().apply {
            addAll(0x0900..0x094F)
            addAll(0x0966..0x096F)
            addAll(0x0671..0x06D3)
            addAll(0x06F0..0x06F9)
        },
        "arab" to HashSet<Int>().apply {
            addAll(0x0620..0x064A)
            addAll(0x0660..0x0669)
        },
        "hebrew" to HashSet<Int>().apply {
            addAll(0x05D0..0x05EA)
        }
    )

    @Synchronized
    fun getKey(shaderGroup: ShaderGroup) = textKeyMap[shaderGroup]
    @Synchronized
    fun setKey(shaderGroup: ShaderGroup, key: HudTextData) {
        textKeyMap[shaderGroup] = key
    }

    private lateinit var unifont: List<Pair<Int, ByteArray>>

    override fun start() {
        loadDefaultBitmap()
        unifont = InputStreamReader(PLUGIN.getResource("unifont.hex").ifNull("unifont.hex not found.")).buffered().use {
            fun String.toBitmap(): ByteArray {
                val byteArray = ByteArray(length * 4)
                var t = 0
                forEach { c ->
                    val char = c.digitToInt(16)
                    for (i in (0..<4).reversed()) {
                        byteArray[t++] = ((char shr i) and 1).toByte()
                    }
                }
                return byteArray
            }
            it.readLines().map { s ->
                val split = s.split(':')
                split[0].toInt(16) to split[1].toBitmap()
            }
        }
        InputStreamReader(PLUGIN.getResource("translatable.json").ifNull("translatable.json not found.")).buffered().use {
            JsonParser.parseReader(it).asJsonObject.entrySet().forEach { e ->
                val map = HashMap<String, String>()
                e.value.asJsonObject.entrySet().forEach { se ->
                    map[se.key] = se.value.asString
                }
                translatableString[e.key] = map
            }
        }
    }
    fun getWidth(codepoint: Int) = textWidthMap[codepoint] ?: 3

    fun getText(name: String) = synchronized(textMap) {
        textMap[name]
    }
    fun translate(locale: String, key: String) = translatableString[locale.uppercase()]?.get(key)

    override fun reload(sender: Audience, resource: GlobalResource) {
        synchronized(this) {
            textMap.clear()
            textWidthMap.clear()
            textKeyMap.clear()
            textCacheMap.clear()
        }
        val assetsFolder = DATA_FOLDER.subFolder("assets")
        val fontFolder = DATA_FOLDER.subFolder("fonts")

        val defaultArray = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("type", "space")
                add("advances", JsonObject().apply {
                    addProperty(" ", 4)
                })
            })
        }
        val fontConfig = PluginConfiguration.FONT.create()
        val configScale = fontConfig.getInt("scale", 16)
        val configHeight = fontConfig.getInt("height", 9)
        val configAscent = fontConfig.getInt("ascent", 8).coerceAtMost(configHeight)

        val defaultProvider = if (!fontConfig.getBoolean("use-unifont")) {
            JavaBitmapProvider(File(DATA_FOLDER, ConfigManagerImpl.defaultFontName).run {
                (if (exists()) runCatching {
                    inputStream().buffered().use {
                        Font.createFont(Font.TRUETYPE_FONT, it)
                    }
                }.getOrNull() else null) ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().font
            }.deriveFont(configScale.toFloat()))
        } else {
            UnifontBitmapProvider(configScale)
        }
        val parseDefault = parseFont("",  "default_$configScale", defaultProvider, configScale, resource.textures, emptyMap(), fontConfig.getStringList("include"),ConditionBuilder.alwaysTrue, fontConfig.getBoolean("merge-default-bitmap", true))
        val heightMultiply = configHeight.toDouble() / parseDefault.height.toDouble()
        parseDefault.charWidth.forEach {
            textWidthMap[it.key] = Math.round(it.value.toDouble() * heightMultiply).toInt()
        }
        parseDefault.array.forEach {
            defaultArray.add(JsonObject().apply {
                addProperty("type", "bitmap")
                addProperty("file", "$NAME_SPACE_ENCODED:${it.file.substringBefore('.').encodeFolder()}/${it.file}")
                addProperty("ascent", configAscent)
                addProperty("height", configHeight)
                add("chars", it.chars)
            })
        }
        PackGenerator.addTask(ArrayList(resource.font).apply {
            add(KeyResource.default.encodeFolder())
            add("${KeyResource.default}.json")
        }) {
            JsonObject().apply {
                add("providers", defaultArray)
            }.toByteArray()
        }

        DATA_FOLDER.subFolder("texts").forEachAllYaml(sender) { file, s, section ->
            runWithExceptionHandling(sender, "Unable to load this text: $s in ${file.name}") {
                val fontDir = section.getString("file")?.let {
                    File(fontFolder, it).ifNotExist("this file doesn't exist: $it")
                }
                val scale = section.getInt("scale", 16)

                val provider = if (!section.getBoolean("use-unifont")) {
                    JavaBitmapProvider((fontDir?.inputStream()?.buffered()?.use {
                        Font.createFont(Font.TRUETYPE_FONT, it)
                    } ?: BufferedImage(
                        1,
                        1,
                        BufferedImage.TYPE_INT_ARGB
                    ).createGraphics().font).deriveFont(scale.toFloat()))
                } else {
                    UnifontBitmapProvider(scale)
                }

                val saveName = "${fontDir?.nameWithoutExtension ?: "default"}_$scale"
                textMap.putSync("text", s) {
                    parseFont(
                        file.path,
                        saveName,
                        provider,
                        scale,
                        resource.textures,
                        HashMap<String, LocatedImage>().apply {
                            section.getConfigurationSection("images")
                                ?.forEachSubConfiguration { key, configurationSection ->
                                    put(key, LocatedImage(
                                        File(
                                            assetsFolder,
                                            configurationSection.getString("name").ifNull("image does not set: $key")
                                        )
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
                        },
                        section.getStringList("include"),
                        section.toConditions(),
                        section.getBoolean("merge-default-bitmap")
                    )
                }
            }
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
                            defaultBitmapImageMap[char] = image.getSubimage(width * (i2++), height * i1, width, height)
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

    private class CharImage(
        val codepoint: Int,
        val image: BufferedImage
    ): Comparable<CharImage> {
        override fun compareTo(other: CharImage): Int {
            return codepoint.compareTo(other.codepoint)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharImage

            return codepoint == other.codepoint
        }

        override fun hashCode(): Int {
            return codepoint.hashCode()
        }

    }

    private interface FontBitmapProvider {
        val height: Int
        fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit)
    }

    private class JavaBitmapProvider(private val font: Font): FontBitmapProvider {
        override val height: Int
            get() = (font.size.toDouble() * 1.4).roundToInt()
        override fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit) {
            val width = font.size
            unicodeRange.filter { char ->
                font.canDisplay(char) && filter(char)
            }.forEachAsync { char ->
                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).processFont(char, font) ?: return@forEachAsync
                block(CharImage(char, image))
            }
        }
    }
    private class UnifontBitmapProvider(private val scale: Int): FontBitmapProvider {
        override val height: Int
            get() = scale
        override fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit) {
            unifont.filter {
                filter(it.first)
            }.forEachAsync {
                val byteImage = it.second.hexToImage()
                val imageWidth = (byteImage.width.toDouble() / 16 * scale).roundToInt()
                val scaledImage = byteImage.getScaledInstance(imageWidth, scale, BufferedImage.SCALE_SMOOTH)

                BufferedImage(imageWidth, scale, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        drawImage(scaledImage, 0, 0, null)
                        dispose()
                    }
                }.fontSubImage()?.let { image ->
                    block(CharImage(it.first, image))
                }
            }
        }
    }

    private fun parseFont(
        path: String,
        saveName: String,
        fontProvider: FontBitmapProvider,
        scale: Int,
        imageSaveFolder: List<String>,
        images: Map<String, LocatedImage>,
        supportedLanguage: List<String>,
        condition: ConditionBuilder,
        mergeDefaultBitmap: Boolean
    ): HudText {
        return synchronized(textCacheMap) {
            textCacheMap[saveName]?.let { old ->
                HudText(path, saveName, old.height, old.array, old.images, old.charWidth, old.conditions)
            }
        } ?: run {
            val height = fontProvider.height
            val pairMap = TreeMap<Int, MutableSet<CharImage>>()
            val charWidthMap = HashMap<Int, Int>()
            fun addImage(image: CharImage) {
                synchronized(pairMap) {
                    pairMap.computeIfAbsent(image.image.width) {
                        TreeSet()
                    }.add(image)
                }
                synchronized(charWidthMap) {
                    charWidthMap[image.codepoint] = image.image.width
                }
            }
            if (mergeDefaultBitmap) defaultBitmapImageMap.entries.toList().forEachAsync {
                val mul = (scale.toDouble() / it.value.width)
                val newWidth = (it.value.width * mul).roundToInt()
                val newHeight = (it.value.height * mul).roundToInt()
                BufferedImage(scale, height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        drawImage(it.value.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH), 0, height - newHeight, null)
                        dispose()
                    }
                }.fontSubImage()?.let { resizedImage ->
                    addImage(CharImage(it.key, resizedImage))
                }
            }
            val supportedCodepoint = HashSet<Int>()
            supportedLanguage.forEach {
                languageCodepointRange[it]?.let { lang ->
                    supportedCodepoint.addAll(lang)
                }
            }
            var filter = { i: Int ->
                !charWidthMap.containsKey(i)
            }
            if (supportedCodepoint.isNotEmpty()) {
                supportedCodepoint.addAll(defaultLatin)
                val old = filter
                filter = {
                    old(it) && supportedCodepoint.contains(it)
                }
            }
            fontProvider.provide(filter) {
                addImage(it)
            }
            val textList = ArrayList<HudTextArray>()
            var i = 0
            images.forEach {
                PackGenerator.addTask(ArrayList(imageSaveFolder).apply {
                    val encode = "glyph_${it.key}".encodeKey()
                    add(encode.encodeFolder())
                    add("$encode.png")
                }) {
                    it.value.image.image.toByteArray()
                }
            }
            pairMap.forEach {
                val width = it.key
                fun save(list: List<CharImage>) {
                    val encode = "text_${saveName}_${++i}".encodeKey()
                    val name = "$encode.png"
                    val json = JsonArray()
                    list.split(CHAR_LENGTH).forEach { subList ->
                        json.add(subList.joinToString("") { pair ->
                            pair.codepoint.parseChar()
                        })
                    }
                    PackGenerator.addTask(ArrayList(imageSaveFolder).apply {
                        add(encode.encodeFolder())
                        add(name)
                    }) {
                        BufferedImage(width * list.size.coerceAtMost(CHAR_LENGTH), height * (((list.size - 1) / CHAR_LENGTH) + 1), BufferedImage.TYPE_INT_ARGB).apply {
                            createGraphics().run {
                                composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                                list.forEachIndexed { index, pair ->
                                    drawImage(pair.image, width * (index % CHAR_LENGTH), height * (index / CHAR_LENGTH), null)
                                }
                                dispose()
                            }
                        }.toByteArray()
                    }
                    textList.add(HudTextArray(name, json))
                }
                it.value.toList().split(CHAR_LENGTH * CHAR_LENGTH).forEach { target ->
                    if (target.size % CHAR_LENGTH == 0 || target.size < CHAR_LENGTH) {
                        save(target)
                    } else {
                        val split = target.split(CHAR_LENGTH)
                        save(split.subList(0, split.lastIndex).sum())
                        save(split.last())
                    }
                }
            }
            val result = HudText(path, saveName, height, textList, images, charWidthMap, condition)
            synchronized(textCacheMap) {
                textCacheMap[saveName] = result
            }
            result
        }
    }

    override fun postReload() {
        textKeyMap.clear()
    }

    override fun end() {
    }
}