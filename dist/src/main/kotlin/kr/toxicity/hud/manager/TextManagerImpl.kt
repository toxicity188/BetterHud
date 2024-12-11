package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import kr.toxicity.hud.api.manager.TextManager
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.element.TextElement
import kr.toxicity.hud.image.LocatedImage
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.CharWidth
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.ImageCharWidth
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object TextManagerImpl : BetterHudManager, TextManager {

    private class TextCache(
        val name: String,
        val imagesName: Set<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TextCache

            if (name != other.name) return false
            if (!imagesName.containsAll(other.imagesName) || imagesName.size != other.imagesName.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            imagesName.forEach {
                result = 31 * result + it.hashCode()
            }
            return result
        }
    }

    private const val CHAR_LENGTH = 16

    @Volatile
    private var fontIndex = 0

    private val textMap = HashMap<String, TextElement>()
    private val textCacheMap = HashMap<TextCache, TextElement>()

    private val textWidthMap = HashMap<Int, Int>()
    private val textKeyMap = ConcurrentHashMap<HudLayout.Identifier, BackgroundKey>()

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
            addAll(0x4E00..0x9FFF) // CJK Unified Ideographs
            addAll(0xFF00..0xFFFE) // Fullwidth Forms
            addAll(0x3000..0x306F) // CJK Symbols and Punctuation
            addAll(0x2000..0x206F) // General Punctuation
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

    private val FRC = FontRenderContext(null, true, true)

    @Synchronized
    fun getKey(shaderGroup: HudLayout.Identifier) = textKeyMap[shaderGroup]
    @Synchronized
    fun setKey(shaderGroup: HudLayout.Identifier, key: BackgroundKey) {
        textKeyMap[shaderGroup] = key
    }

    private lateinit var unifont: List<Pair<Int, ByteArray>>

    override fun start() {
        loadDefaultBitmap()
        unifont = InputStreamReader(BOOTSTRAP.resource("unifont.hex").ifNull("unifont.hex not found.")).buffered().use {
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
        InputStreamReader(BOOTSTRAP.resource("translatable.json").ifNull("translatable.json not found.")).buffered().use {
            parseJson(it).asJsonObject.entrySet().forEach { e ->
                val map = HashMap<String, String>()
                e.value.asJsonObject.entrySet().forEach { se ->
                    map[se.key] = se.value.asString
                }
                translatableString[e.key] = map
            }
        }
    }
    fun getWidth(codepoint: Int) = textWidthMap[codepoint]

    fun getText(name: String) = synchronized(textMap) {
        textMap[name]
    }
    fun translate(locale: String, key: String) = translatableString[locale.uppercase()]?.get(key)

    private fun createFont(dir: File?, scale: Int, useUnifont: Boolean) = if (useUnifont) {
        JavaBitmapProvider((dir?.inputStream()?.buffered()?.use {
            runCatching {
                Font.createFont(Font.TRUETYPE_FONT, it)
            }.getOrElse { e ->
                warn(
                    "Unable to load this font: ${dir.path}",
                    "",
                    "If you're using Ubuntu, try this command:",
                    "apt install fontconfig"
                )
                throw e
            }
        } ?: BufferedImage(
            1,
            1,
            BufferedImage.TYPE_INT_ARGB
        ).createGraphics().font).deriveFont(scale.toFloat()))
    } else {
        UnifontBitmapProvider(scale)
    }


    override fun reload(sender: Audience, resource: GlobalResource) {
        synchronized(this) {
            fontIndex = 0
            textMap.clear()
            textWidthMap.clear()
            textKeyMap.clear()
            textCacheMap.clear()
        }
        val assetsFolder = DATA_FOLDER.subFolder("assets")
        val fontFolder = DATA_FOLDER.subFolder("fonts")

        val defaultArray = jsonArrayOf(
            jsonObjectOf(
                "type" to "space",
                "advances" to jsonObjectOf(
                    " " to 4
                )
            )
        )
        val fontConfig = PluginConfiguration.FONT.create()
        val configScale = fontConfig.getAsInt("scale", 16)
        val configHeight = fontConfig.getAsInt("height", 9)
        val configAscent = fontConfig.getAsInt("ascent", 8).coerceAtMost(configHeight)

        val defaultProvider = createFont(
            File(DATA_FOLDER, ConfigManagerImpl.defaultFontName).takeIf { it.exists() },
            configScale,
            fontConfig.getAsBoolean("use-unifont", false)
        )
        val parseDefault = parseTTFFont("",  "default_$configScale", defaultProvider, configScale, resource.textures, emptyMap(), fontConfig.get("include")?.asArray()?.map {
            it.asString()
        } ?: emptyList(), fontConfig.getAsBoolean("merge-default-bitmap", true), fontConfig)
        parseDefault.charWidth.forEach {
            textWidthMap[it.key] = (it.value.width.toDouble() * configHeight / it.value.height.toDouble()).roundToInt()
        }
        parseDefault.array.forEach {
            defaultArray += jsonObjectOf(
                "type" to "bitmap",
                "file" to "$NAME_SPACE_ENCODED:${it.file}",
                "ascent" to configAscent,
                "height" to configHeight,
                "chars" to it.chars
            )
        }
        PackGenerator.addTask(resource.font + "${ConfigManagerImpl.key.defaultKey.value()}.json") {
            jsonObjectOf("providers" to defaultArray).toByteArray()
        }

        DATA_FOLDER.subFolder("texts").forEachAllYaml(sender) { file, s, section ->
            runWithExceptionHandling(sender, "Unable to load this text: $s in ${file.name}") {
                val fontDir = section["file"]?.asString()?.let {
                    File(fontFolder, it).ifNotExist("this file doesn't exist: $it")
                }
                val scale = section.getAsInt("scale", 16)

                val provider = createFont(
                    fontDir,
                    scale,
                    section.getAsBoolean("use-unifont", false)
                )

                textMap.putSync("text", s) {
                    when (section.getAsString("type", "ttf").lowercase()) {
                        "ttf" -> {
                            parseTTFFont(
                                file.path,
                                "${fontDir?.nameWithoutExtension ?: "default"}_$scale",
                                provider,
                                scale,
                                resource.textures,
                                TreeMap<String, LocatedImage>().apply {
                                    section["images"]?.asObject()
                                        ?.forEachSubConfiguration { key, yamlObject ->
                                            put(key, LocatedImage(
                                                File(
                                                    assetsFolder,
                                                    yamlObject["name"]?.asString().ifNull("image does not set: $key")
                                                )
                                                    .ifNotExist("this image doesn't exist: $key")
                                                    .toImage()
                                                    .removeEmptyWidth()
                                                    .ifNull("invalid image: $key"),
                                                PixelLocation(yamlObject),
                                                yamlObject.getAsDouble("scale", 1.0).apply {
                                                    if (this <= 0.0) throw RuntimeException("scale cannot be <= 0: $key")
                                                }
                                            ))
                                        }
                                },
                                section["include"]?.asArray()?.map {
                                    it.asString()
                                } ?: emptyList(),
                                section.getAsBoolean("merge-default-bitmap", false),
                                section,
                            )
                        }
                        "bitmap" -> {
                            parseBitmapFont(
                                file.path,
                                s,
                                resource.textures,
                                section["chars"].ifNull("Unable to find 'chars' array.").asObject().mapSubConfiguration { _, obj ->
                                    BitmapData(
                                        obj.get("codepoints").ifNull("codepoints value not set.").asArray().map { y ->
                                            y.asString()
                                        },
                                        obj.get("file").ifNull("file value not set.").asString()
                                    )
                                },
                                section
                            )
                        }
                        else -> throw RuntimeException("Unsupported type: only ttf or bitmap supported.")
                    }

                }
            }
        }
    }

    private fun loadDefaultBitmap() {
        defaultBitmapImageMap.clear()
        BOOTSTRAP.resource("minecraft_default.json")?.let {
            runCatching {
                InputStreamReader(it).buffered().use { reader ->
                    parseJson(reader)
                }.asJsonObject.getAsJsonArray("providers").forEachIndexed { debugIndex, element ->
                    val obj = element.asJsonObject
                    val imageName = (obj.getAsJsonPrimitive("file") ?: run {
                        warn("Unable to find file name in this sector: $debugIndex")
                        return
                    }).asString.run {
                        if (contains('/')) substringAfterLast('/') else this
                    }
                    val image = runCatching {
                        BOOTSTRAP.resource(imageName)?.buffered()?.toImage() ?: run {
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
                            defaultBitmapImageMap[char] = image.getSubimage(width * i2++, height * i1, width, height)
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



    private class JavaBitmapProvider(private val targetFont: Font): FontBitmapProvider {
        override val height: Int
            get() = (targetFont.size.toDouble() * 1.4).roundToInt()
        override fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit) {
            val h = height
            unicodeRange.filter { char ->
                targetFont.canDisplay(char) && filter(char)
            }.forEachAsync { char ->
                BufferedImage(
                    targetFont.size,
                    h,
                    BufferedImage.TYPE_INT_ARGB
                ).apply {
                    createGraphics().run {
                        fill(targetFont.createGlyphVector(FRC, char.parseChar()).getOutline(0F, targetFont.size.toFloat()))
                        dispose()
                    }
                }.removeEmptyWidth()?.let {
                    block(CharImage(char, it.image))
                }
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

    private fun parseBitmapFont(
        path: String,
        saveName: String,
        imageSaveFolder: List<String>,
        data: List<BitmapData>,
        yamlObject: YamlObject,
    ): TextElement {
        return synchronized(textCacheMap) {
            textCacheMap[TextCache(saveName, emptySet())]?.let { old ->
                TextElement(path, saveName, old.array, old.charWidth, old.imageCharWidth, yamlObject)
            }
        } ?: run {
            val saveFontName = synchronized (this) {
                "font${++fontIndex}"
            }
            val charWidthMap = HashMap<Int, CharWidth>()
            val textArray = ArrayList<HudTextArray>()
            val assetsFolder = DATA_FOLDER.subFolder("assets")
            data.forEachIndexed { i, d ->
                val file = File(assetsFolder, d.file.replace('/', File.separatorChar))
                    .ifNotExist("Unable to find this asset file: ${d.file}")
                    .toImage()
                if (d.codepoints.isEmpty()) throw RuntimeException("Codepoint is empty.")
                if (file.height % d.codepoints.size != 0) throw RuntimeException("Image height ${file.height} cannot be divided to ${d.codepoints.size}.")
                val codepointStream = d.codepoints.map { s ->
                    s.codePoints().toArray().apply {
                        if (isEmpty()) throw RuntimeException("Codepoint is empty.")
                        if (file.width % size != 0) throw RuntimeException("Image width ${file.width} cannot be divided to ${size}.")
                    }
                }
                val distinct = codepointStream.map { c ->
                    c.size
                }.distinct()
                if (distinct.size != 1) throw RuntimeException("Codepoint length of bitmap does not same.")
                val width = distinct[0]
                val encode = "text_${saveFontName}_${i + 1}".encodeKey()
                val name = "$encode.png"

                val divWidth = file.width / width
                val divHeight = file.height / d.codepoints.size

                codepointStream.forEachIndexed { h, s ->
                    s.forEachIndexed { w, i ->
                        charWidthMap[i] = CharWidth(
                            file.getSubimage(w * divWidth, h * divHeight, divWidth, divHeight)
                                    .removeEmptyWidth()?.let {
                                        it.xOffset + it.image.width
                                    } ?: 0,
                            divHeight
                        )
                    }
                }

                PackGenerator.addTask(imageSaveFolder + name) {
                    file.toByteArray()
                }
                textArray += HudTextArray(
                    name,
                    jsonArrayOf(*d.codepoints.toTypedArray()),
                    divHeight
                )
            }
            TextElement(
                path,
                saveName,
                textArray,
                charWidthMap,
                mapOf(),
                yamlObject
            )
        }
    }

    private data class BitmapData(
        val codepoints: List<String>,
        val file: String
    )

    private fun parseTTFFont(
        path: String,
        saveName: String,
        fontProvider: FontBitmapProvider,
        scale: Int,
        imageSaveFolder: List<String>,
        images: Map<String, LocatedImage>,
        supportedLanguage: List<String>,
        mergeDefaultBitmap: Boolean,
        yamlObject: YamlObject
    ): TextElement {
        return synchronized(textCacheMap) {
            textCacheMap[TextCache(saveName, images.keys)]?.let { old ->
                TextElement(path, saveName, old.array, old.charWidth, old.imageCharWidth, yamlObject)
            }
        } ?: run {
            val saveFontName = synchronized (this) {
                "font${++fontIndex}"
            }
            val height = fontProvider.height
            val pairMap = TreeMap<Int, MutableSet<CharImage>>()
            val charWidthMap = HashMap<Int, CharWidth>()
            fun addImage(image: CharImage) {
                synchronized(pairMap) {
                    pairMap.computeIfAbsent(image.image.width) {
                        TreeSet()
                    }.add(image)
                }
                synchronized(charWidthMap) {
                    charWidthMap[image.codepoint] = CharWidth(
                        image.image.width,
                        height
                    )
                }
            }
            if (mergeDefaultBitmap) defaultBitmapImageMap.entries.toList().forEachAsync {
                val mul = scale.toDouble() / it.value.width
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
            var imageStart = TEXT_IMAGE_START_CODEPOINT
            val imageCharWidthMap = HashMap<Int, ImageCharWidth>()
            images.forEach {
                imageCharWidthMap[++imageStart] = ImageCharWidth(
                    it.key,
                    "$NAME_SPACE_ENCODED:${"glyph_${it.key}".encodeKey()}.png",
                    it.value.location,
                    it.value.scale,
                    it.value.image.image.height,
                    it.value.image.image.height
                )
                PackGenerator.addTask(imageSaveFolder + "${"glyph_${it.key}".encodeKey()}.png") {
                    it.value.image.image.toByteArray()
                }
            }
            pairMap.forEach {
                val width = it.key
                fun save(list: List<CharImage>) {
                    val encode = "text_${saveFontName}_${++i}".encodeKey()
                    val name = "$encode.png"
                    val json = JsonArray()
                    list.split(CHAR_LENGTH).forEach { subList ->
                        json.add(subList.joinToString("") { pair ->
                            pair.codepoint.parseChar()
                        })
                    }
                    PackGenerator.addTask(imageSaveFolder + name) {
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
                    textList.add(HudTextArray(name, json, height))
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
            val result = TextElement(path, saveName, textList, charWidthMap, imageCharWidthMap, yamlObject)
            synchronized(textCacheMap) {
                textCacheMap[TextCache(saveName, images.keys)] = result
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
