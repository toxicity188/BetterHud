package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.text.HudText
import kr.toxicity.hud.text.HudTextArray
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.ceil

object TextManager: BetterHudManager {

    private const val CHAR_LENGTH = 16

    private val antialiasing = RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    private val fractionalMetrics = RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    private val textMap = HashMap<String, HudText>()

    private val textWidthMap = HashMap<Char, Int>()
    private val textKeyMap = mutableMapOf<ShaderGroup, Key>()

    fun getKey(shaderGroup: ShaderGroup) = textKeyMap[shaderGroup]
    fun setKey(shaderGroup: ShaderGroup, key: Key) {
        textKeyMap[shaderGroup] = key
    }

    override fun start() {
    }
    fun getWidth(char: Char) = textWidthMap[char] ?: 3

    fun getText(name: String) = textMap[name]

    override fun reload(resource: GlobalResource) {
        textMap.clear()
        textWidthMap.clear()
        textKeyMap.clear()
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
                textMap[s] = parseFont(s, saveName, fontFile, scale, globalSaveFolder, section.toConditions())
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
        val parseDefault = parseFont("default", "default", BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics().font.deriveFont(12F), 12, resource.textures.subFolder("font"), ConditionBuilder.alwaysTrue)
        parseDefault.charWidth.forEach {
            textWidthMap[it.key] = ceil(it.value.toDouble() / 2).toInt()
        }
        parseDefault.array.forEach {
            defaultArray.add(JsonObject().apply {
                addProperty("type", "bitmap")
                addProperty("file", "$NAME_SPACE:font/default/${it.file}")
                addProperty("ascent", 8)
                addProperty("height", 9)
                add("chars", it.chars)
            })
        }
        JsonObject().apply {
            add("providers", defaultArray)
        }.save(resource.font.subFile("default.json"))
    }

    private fun parseFont(s: String, saveName: String, fontFile: Font, scale: Int, imageSaveFolder: File, condition: ConditionBuilder): HudText {
        val height = (scale.toDouble() * 1.4).toInt()
        val pairMap = HashMap<Int, MutableList<Pair<Char, BufferedImage>>>()
        (Char.MIN_VALUE..Char.MAX_VALUE).forEach { char ->
            if (fontFile.canDisplay(char)) {
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
                    pairMap.getOrPut(it.width) {
                        ArrayList()
                    }.add(char to it)
                }
            }
        }
        val textList = ArrayList<HudTextArray>()
        val saveFolder = imageSaveFolder.subFolder(saveName)
        var i = 0
        pairMap.forEach {
            val width = it.key
            fun save(list: List<Pair<Char, BufferedImage>>) {
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
        return HudText(s, saveName, height, textList, HashMap<Char, Int>().apply {
            pairMap.forEach { entry ->
                entry.value.forEach { pair ->
                    put(pair.first, entry.key)
                }
            }
        }, condition)
    }

    override fun end() {
    }
}