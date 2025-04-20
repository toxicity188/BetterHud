package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import kr.toxicity.hud.api.manager.ConfigManager
import kr.toxicity.hud.api.manager.TextManager
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.element.TextElement
import kr.toxicity.hud.image.LocatedImage
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.text.BackgroundKey
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.text.ImageTextScale
import kr.toxicity.hud.text.TextScale
import kr.toxicity.hud.util.*
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

    override val managerName: String = "Text"
    override val supportExternalPacks: Boolean = true

    private data class TextCache(
        val name: String,
        val imagesName: Set<String>,
        val supportLanguage: Set<String>?
    )

    private const val CHAR_LENGTH = 16

    private var fontIndex = 0

    private val textMap = HashMap<String, TextElement>()
    private val textCacheMap = HashMap<TextCache, TextElement>()

    private val textWidthMap = intMapOf()
    private val textKeyMap = ConcurrentHashMap<HudLayout.Identifier, BackgroundKey>()

    private val defaultBitmapImageMap = intKeyMapOf<BufferedImage>()
    private val translatableString = HashMap<String, Map<String, String>>()

    private val unicodeRange = 0..0x10FFFF

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
            addAll(0x20..0x25)
            addAll(0x27..0x3A)
            addAll(0x3C..0x3E)
            add(0x40)
            addAll(0x5B..0x5F)
            add(0x7C)
            add(0xA0)
            add(0xA9)
            addAll(0xE01..0xE3A)
            addAll(0xE40..0xE4E)
            addAll(0x2010..0x2011)
            addAll(0x2013..0x2014)
            addAll(0x2018..0x2019)
            addAll(0x201C..0x201D)
            add(0x2026)
            add(0x2030)
            addAll(0x2032..0x2033)
            add(0x20AC)
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
        },
        "turkish" to HashSet<Int>().apply {
            addAll(0x20..0x5F)
            addAll(0x61..0x70)
            addAll(0x72..0x76)
            addAll(0x79..0x7A)
            add(0x7C)
            add(0xA0)
            add(0xA7)
            add(0xA9)
            add(0xC7)
            add(0xD6)
            add(0xDC)
            add(0xE7)
            add(0xF6)
            add(0xFC)
            addAll(0x11E..0x11F)
            addAll(0x130..0x131)
            addAll(0x15E..0x15F)
            addAll(0x2010..0x2011)
            addAll(0x2013..0x2014)
            addAll(0x2018..0x2019)
            addAll(0x201C..0x201D)
            addAll(0x2020..0x2021)
            add(0x2026)
            add(0x2030)
            addAll(0x2032..0x2033)
            add(0x20AC)
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
        unifont = InputStreamReader(BOOTSTRAP.resource("unifont.hex").ifNull { "unifont.hex not found." }).buffered().use {
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
        InputStreamReader(BOOTSTRAP.resource("translatable.json").ifNull { "translatable.json not found." }).buffered().use {
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

    private fun createFont(dir: File?, scale: Int, useUnifont: Boolean) = if (!useUnifont) {
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


    override fun preReload() {
        fontIndex = 0
        textMap.clear()
        textWidthMap.clear()
        textKeyMap.clear()
        textCacheMap.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        val assetsFolder = workingDirectory.subFolder("assets")
        val fontFolder = workingDirectory.subFolder("fonts")

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
        val configDiv = 8.0 / configScale

        val defaultProvider = createFont(
            File(DATA_FOLDER, ConfigManagerImpl.defaultFontName).takeIf { it.exists() },
            configScale,
            fontConfig.getAsBoolean("use-unifont", false)
        )
        val parseDefault = parseTTFFont("", "default_$configScale", configScale, defaultProvider, resource.textures, emptyMap(), fontConfig.get("include")?.asArray()?.map {
            it.asString()
        }?.toSet(), fontConfig.getAsBoolean("merge-default-bitmap", true), fontConfig)()
        parseDefault.charWidth.forEach {
            textWidthMap[it.key] = (it.value * configDiv).normalizedWidth
        }
        parseDefault.array.forEach {
            defaultArray += jsonObjectOf(
                "type" to "bitmap",
                "file" to "$NAME_SPACE_ENCODED:${it.file}",
                "ascent" to it.ascent(configDiv) + 7,
                "height" to (configDiv * it.height).roundToInt(),
                "chars" to it.chars
            )
        }
        PackGenerator.addTask(resource.font + "${ConfigManagerImpl.key.defaultKey.value()}.json") {
            jsonObjectOf("providers" to defaultArray).toByteArray()
        }

        val suppliers = mutableListOf<TextSupplier>()
        workingDirectory.subFolder("texts").forEachAllYaml(info.sender) { file, s, section ->
            runCatching {
                val fontDir = section["file"]?.asString()?.let {
                    File(fontFolder, it).ifNotExist { "this file doesn't exist: $it" }
                }
                val scale = section.getAsInt("scale", 16)

                val provider = createFont(
                    fontDir,
                    scale,
                    section.getAsBoolean("use-unifont", false)
                )
                suppliers += when (section.getAsString("type", "ttf").lowercase()) {
                    "ttf" -> {
                        parseTTFFont(
                            s,
                            "${fontDir?.nameWithoutExtension ?: "default"}_$scale",
                            scale,
                            provider,
                            resource.textures,
                            TreeMap<String, LocatedImage>().apply {
                                section["images"]?.asObject()
                                    ?.forEachSubConfiguration { key, yamlObject ->
                                        put(key, LocatedImage(
                                            File(
                                                assetsFolder,
                                                yamlObject["name"]?.asString().ifNull { "image does not set: $key" }
                                            )
                                                .ifNotExist { "this image doesn't exist: $key" }
                                                .toImage()
                                                .removeEmptyWidth()
                                                .ifNull { "invalid image: $key" },
                                            PixelLocation(yamlObject),
                                            yamlObject.getAsDouble("scale", 1.0).apply {
                                                if (this <= 0.0) throw RuntimeException("scale cannot be <= 0: $key")
                                            }
                                        ))
                                    }
                            },
                            section["include"]?.asArray()?.map {
                                it.asString()
                            }?.toSet(),
                            section.getAsBoolean("merge-default-bitmap", false),
                            section,
                        )
                    }
                    "bitmap" -> {
                        parseBitmapFont(
                            workingDirectory,
                            s,
                            resource.textures,
                            section["chars"].ifNull { "Unable to find 'chars' array." }.asObject().mapSubConfiguration { _, obj ->
                                BitmapData(
                                    obj.get("codepoints").ifNull { "codepoints value not set." }.asArray().map { y ->
                                        y.asString()
                                    },
                                    obj.get("file").ifNull { "file value not set." }.asString(),
                                    obj.getAsInt("ascent", 0)
                                )
                            },
                            section
                        )
                    }
                    else -> throw RuntimeException("Unsupported type: only ttf or bitmap supported.")
                }
            }.handleFailure(info) {
                "Unable to load this text: $s in ${file.name}"
            }
        }
        suppliers.forEachAsync { s ->
            textMap.putSync("text") {
                s()
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
                        e.handle("Unable to load this image: $imageName")
                        return
                    }
                    val array = obj.getAsJsonArray("chars")
                    val height = image.height / array.size()
                    array.forEachIndexed { i1, charElement ->
                        val str = charElement.asString
                        val width = image.width / str.length
                        var i2 = 0
                        str.codePoints().forEach { char ->
                            image.getSubimage(width * i2++, height * i1, width, height).removeEmptyWidth()?.let { i ->
                                defaultBitmapImageMap[char] = i.image
                            }
                        }
                    }
                }
            }.handleFailure {
                "Unable to parse minecraft_default.json"
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
        override val height: Int = (targetFont.size.toDouble() * 1.4).roundToInt()
        override fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit) {
            unicodeRange.filter(filter).forEachAsync { char ->
                if (targetFont.canDisplay(char)) BufferedImage(
                    targetFont.size,
                    height,
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

    private class UnifontBitmapProvider(scale: Int): FontBitmapProvider {
        override val height: Int = scale
        override fun provide(filter: (Int) -> Boolean, block: (CharImage) -> Unit) {
            unifont.filter {
                filter(it.first)
            }.forEachAsync {
                it.second.hexToImage().removeEmptyWidth()?.let { image ->
                    block(CharImage(it.first, image.image))
                }
            }
        }
    }

    private fun parseBitmapFont(
        workingDirectory: File,
        saveName: String,
        imageSaveFolder: List<String>,
        data: List<BitmapData>,
        yamlObject: YamlObject,
    ): TextSupplier {
        return synchronized(textCacheMap) {
            textCacheMap[TextCache(saveName, emptySet(), emptySet())]?.let { old ->
                TextSupplier {
                    TextElement(saveName, null, old.array, old.charWidth, old.imageTextScale, yamlObject)
                }
            }
        } ?: run {
            val saveFontName = "font${++fontIndex}"
            TextSupplier {
                val charWidthMap = intKeyMapOf<TextScale>()
                val textArray = ArrayList<HudTextArray>()
                val assetsFolder = workingDirectory.subFolder("assets")
                debug(ConfigManager.DebugLevel.ASSETS, "Generating bitmap text $saveName...")
                data.forEachIndexed { i, d ->
                    val file = File(assetsFolder, d.file.replace('/', File.separatorChar))
                        .ifNotExist { "Unable to find this asset file: ${d.file}" }
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
                    val encode = "text_${saveFontName}_${i + 1}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)
                    val name = "$encode.png"

                    val divWidth = file.width / width
                    val divHeight = (file.height / d.codepoints.size)

                    codepointStream.forEachIndexed { h, s ->
                        s.forEachIndexed { w, i ->
                            charWidthMap[i] = TextScale(
                                file.getSubimage(w * divWidth, h * divHeight, divWidth, divHeight)
                                    .removeEmptyWidth()?.let {
                                        it.xOffset + it.image.width
                                    }?.toDouble() ?: 0.0,
                                divHeight.toDouble()
                            )
                        }
                    }

                    PackGenerator.addTask(imageSaveFolder + name) {
                        file.toByteArray()
                    }
                    textArray += HudTextArray(
                        name,
                        jsonArrayOf(*d.codepoints.toTypedArray()),
                        divHeight.toDouble(),
                    ) {
                        d.ascent
                    }
                }
                debug(ConfigManager.DebugLevel.ASSETS, "Finalizing bitmap text $saveName...")
                TextElement(
                    saveName,
                    null,
                    textArray,
                    charWidthMap,
                    intKeyMapOf(),
                    yamlObject
                )
            }
        }
    }

    private data class BitmapData(
        val codepoints: List<String>,
        val file: String,
        val ascent: Int
    )

    private data class FontDisplay(val width: Int, val height: Int, val multiplier: Double) : Comparable<FontDisplay> {
        companion object {
            private val comparator = Comparator.comparing { display: FontDisplay ->
                display.width
            }.thenComparing { display: FontDisplay ->
                display.height
            }.thenComparing { display: FontDisplay ->
                display.multiplier
            }
        }

        override fun compareTo(other: FontDisplay): Int = comparator.compare(this, other)
    }

    private fun interface TextSupplier : () -> TextElement

    private fun parseTTFFont(
        internalName: String,
        saveName: String,
        scale: Int,
        fontProvider: FontBitmapProvider,
        imageSaveFolder: List<String>,
        images: Map<String, LocatedImage>,
        supportedLanguage: Set<String>?,
        mergeDefaultBitmap: Boolean,
        yamlObject: YamlObject
    ): TextSupplier {
        return synchronized(textCacheMap) {
            textCacheMap[TextCache(saveName, images.keys, supportedLanguage)]?.let { old ->
                TextSupplier {
                    TextElement(internalName, scale, old.array, old.charWidth, old.imageTextScale, yamlObject)
                }
            }
        } ?: run {
            val saveFontName = "font${++fontIndex}"
            TextSupplier {
                debug(ConfigManager.DebugLevel.ASSETS, "Starting font text $saveName...")
                val pairMap = TreeMap<FontDisplay, MutableSet<CharImage>>()
                val charWidthMap = intKeyMapOf<TextScale>()
                fun addImage(image: CharImage, multiplier: Double = 1.0) {
                    val w = image.image.width
                    val h = image.image.height
                    synchronized(pairMap) {
                        pairMap.computeIfAbsent(FontDisplay(
                            w,
                            h,
                            multiplier
                        )) {
                            TreeSet()
                        }.add(image)
                    }
                    synchronized(charWidthMap) {
                        charWidthMap.computeIfAbsent(image.codepoint) {
                            TextScale(
                                w.toDouble(),
                                h.toDouble()
                            ) * multiplier
                        }
                    }
                }
                if (mergeDefaultBitmap) defaultBitmapImageMap.forEach { (k, v) ->
                    addImage(CharImage(k, v), scale.toDouble() / 8.0)
                }
                var filter = { i: Int ->
                    !charWidthMap.containsKey(i)
                }
                supportedLanguage?.let { lang ->
                    val supportedCodepoint = HashSet(defaultLatin)
                    lang.forEach {
                        languageCodepointRange[it]?.let { lang ->
                            supportedCodepoint += lang
                        }
                    }
                    val old = filter
                    filter = {
                        old(it) && supportedCodepoint.contains(it)
                    }
                }
                fontProvider.provide(filter) {
                    addImage(it, scale.toDouble() / fontProvider.height)
                }
                debug(ConfigManager.DebugLevel.ASSETS, "Generate font text $saveName...")
                val textList = ArrayList<HudTextArray>()
                var i = 0
                var imageStart = TEXT_IMAGE_START_CODEPOINT
                val imageTextScaleMap = intKeyMapOf<ImageTextScale>()
                images.forEach { (k, v) ->
                    val nh = v.image.image.height.toDouble() * v.scale
                    imageTextScaleMap[++imageStart] = ImageTextScale(
                        k,
                        "$NAME_SPACE_ENCODED:${"glyph_${k}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)}.png",
                        v.location,
                        scale - nh.roundToInt(),
                        v.image.image.width.toDouble() * v.scale,
                        nh
                    )
                    PackGenerator.addTask(imageSaveFolder + "${"glyph_${k}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)}.png") {
                        v.image.image.toByteArray()
                    }
                }
                pairMap.forEach { (k, v) ->
                    val (w, h, m) = k
                    fun save(list: List<CharImage>) {
                        val encode = "text_${saveFontName}_${++i}".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)
                        val name = "$encode.png"
                        val json = JsonArray()
                        list.split(CHAR_LENGTH).forEach { subList ->
                            json.add(subList.joinToString("") { pair ->
                                pair.codepoint.parseChar()
                            })
                        }
                        PackGenerator.addTask(imageSaveFolder + name) {
                            BufferedImage(w * list.size.coerceAtMost(CHAR_LENGTH), h * (((list.size - 1) / CHAR_LENGTH) + 1), BufferedImage.TYPE_INT_ARGB).apply {
                                createGraphics().run {
                                    composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                                    list.forEachIndexed { index, pair ->
                                        drawImage(pair.image, w * (index % CHAR_LENGTH), h * (index / CHAR_LENGTH), null)
                                    }
                                    dispose()
                                }
                            }.toByteArray()
                        }
                        textList += HudTextArray(name, json, m * h) { s ->
                            (s * m * h).roundToInt().let {
                                it - (it - 8) / 4 - (scale * s).roundToInt()
                            }
                        }
                    }
                    v.toList().split(CHAR_LENGTH * CHAR_LENGTH).forEach { target ->
                        if (target.size % CHAR_LENGTH == 0 || target.size < CHAR_LENGTH) {
                            save(target)
                        } else {
                            val split = target.split(CHAR_LENGTH)
                            save(split.subList(0, split.lastIndex).sum())
                            save(split.last())
                        }
                    }
                }
                debug(ConfigManager.DebugLevel.ASSETS, "Finalizing font text $saveName... (${charWidthMap.size.withDecimal()} of codepoints)")
                val result = TextElement(internalName, scale, textList, charWidthMap, imageTextScaleMap, yamlObject)
                synchronized(textCacheMap) {
                    textCacheMap[TextCache(internalName, images.keys, supportedLanguage)] = result
                }
                result
            }
        }
    }

    override fun postReload() {
        textKeyMap.clear()
    }

    override fun end() {
    }
}
