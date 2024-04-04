package kr.toxicity.hud.manager

import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HotBarShader
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.util.regex.Pattern

object ShaderManager: BetterHudManager {
    var barColor = BarColor.YELLOW
        private set

    private var index = 0
    private val tagPattern = Pattern.compile("#(?<name>[a-zA-Z]+)")
    private val deactivatePattern = Pattern.compile("//(?<name>[a-zA-Z]+)")
    private val tagBuilders: Map<String, () -> List<String>> = mapOf(
        "CreateConstant" to {
            constants.map {
                "#define ${it.key} ${it.value}"
            }
        },
        "CreateLayout" to {
            ArrayList<String>().apply {
                hudShaders.forEach {
                    addAll(it.value.first)
                }
            }
        }
    )

    private val hudShaders = mutableMapOf<HudShader, Pair<List<String>, Int>>()
    fun addHudShader(shader: HudShader): Int {
        return hudShaders[shader]?.second ?: run {
            index++
            hudShaders[shader] = listOf(
                "case ${index}:",
                "    xGui = ui.x * ${shader.gui.x.toFloat()} / 100.0;",
                "    yGui = ui.y * ${shader.gui.y.toFloat()} / 100.0;",
                "    layer = ${shader.layer};",
                "    outline = ${shader.outline};",
                "    break;"
            ) to index
            index
        }
    }

    private val constants = mutableMapOf<String, String>()

    private val shaderConstants = mapOf(
        "HEIGHT_BIT" to HudImpl.DEFAULT_BIT.toString(),
        "MAX_BIT" to HudImpl.MAX_BIT.toString(),
        "DEFAULT_OFFSET" to "64",
        "ADD_OFFSET" to HudImpl.ADD_HEIGHT.toString()
    )

    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
        constants.clear()
        index = 0
        val file = File(DATA_FOLDER, "shader.yml")
        if (!file.exists()) PLUGIN.saveResource("shader.yml", false)
        runCatching {
            fun getReader(name: String): Pair<String, BufferedReader> {
                return (name to run {
                    val f = File(DATA_FOLDER, name)
                    if (!f.exists()) PLUGIN.saveResource(name, false)
                    runCatching {
                        f.bufferedReader(Charsets.UTF_8)
                    }.getOrNull() ?: throw RuntimeException("plugin jar file has a problem.")
                })
            }
            val shaders = listOf(
                getReader("rendertype_entity_cutout.vsh"),
                getReader("rendertype_entity_translucent_cull.vsh"),
                getReader("rendertype_text.vsh")
            )
            constants += shaderConstants
            val replaceList = mutableSetOf<String>()

            val yaml = file.toYaml()
            barColor = yaml.getString("bar-color")?.let {
                runCatching {
                    BarColor.valueOf(it.uppercase())
                }.getOrNull()
            } ?: BarColor.RED
            fun copy(suffix: String) {
                PLUGIN.getResource("background.png")?.buffered()?.use { input ->
                    resource.bossBar
                        .subFolder("sprites")
                        .subFolder("boss_bar")
                        .subFile("${barColor.name.lowercase()}_$suffix.png")
                        .outputStream()
                        .buffered()
                        .use { output ->
                            input.copyTo(output)
                        }
                }
            }
            copy("background")
            copy("progress")
            PLUGIN.getResource("bars.png")?.buffered()?.use { target ->
                val oldImage = target.toImage()
                val yAxis = 10 * barColor.ordinal
                BufferedImage(oldImage.width, oldImage.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        if (barColor.ordinal > 0) drawImage(oldImage.getSubimage(0, 0, oldImage.width, yAxis), 0, 0, null)
                        drawImage(oldImage.getSubimage(0, yAxis + 10, oldImage.width, oldImage.height - yAxis - 10), 0, yAxis + 10, null)
                        dispose()
                    }
                }.save(File(resource.bossBar, "bars.png"))
            }

            if (yaml.getBoolean("disable-level-text")) replaceList.add("HideExp")
            if (yaml.getBoolean("disable-item-name")) replaceList.add("HideItemName")

            yaml.getConfigurationSection("hotbar")?.let {
                if (it.getBoolean("disable")) {
                    replaceList.add("RemapHotBar")
                    val locations = it.getConfigurationSection("locations").ifNull("locations configuration not set.")
                    (1..10).map { index ->
                        locations.getConfigurationSection(index.toString())?.let { shaderConfig ->
                            HotBarShader(
                                shaderConfig.getConfigurationSection("gui")?.let { gui ->
                                    gui.getDouble("x") to gui.getDouble("y")
                                } ?: (0.0 to 0.0),
                                shaderConfig.getConfigurationSection("pixel")?.let { pixel ->
                                    pixel.getInt("x") to pixel.getInt("y")
                                } ?: (0 to 0),
                            )
                        } ?: HotBarShader.empty
                    }.forEachIndexed { index, hotBarShader ->
                        val i = index + 1
                        constants["HOTBAR_${i}_GUI_X"] = hotBarShader.gui.first.toFloat().toString()
                        constants["HOTBAR_${i}_GUI_Y"] = hotBarShader.gui.second.toFloat().toString()
                        constants["HOTBAR_${i}_PIXEL_X"] = hotBarShader.pixel.first.toFloat().toString()
                        constants["HOTBAR_${i}_PIXEL_Y"] = hotBarShader.pixel.second.toFloat().toString()
                    }
                }
            }

            shaders.forEach { shader ->
                shader.second.use { reader ->
                    File(resource.core, shader.first).bufferedWriter(Charsets.UTF_8).use { writer ->
                        reader.readLines().forEach write@ { string ->
                            var s = string
                            val deactivateMatcher = deactivatePattern.matcher(s)
                            if (deactivateMatcher.find() && replaceList.contains(deactivateMatcher.group("name"))) {
                                s = deactivateMatcher.replaceAll("")
                            }
                            val tagMatcher = tagPattern.matcher(s)
                            if (tagMatcher.find()) {
                                tagBuilders[tagMatcher.group("name")]?.let {
                                    val space = "".padStart(s.count { it == ' ' }, ' ')
                                    it().forEach { methodString ->
                                        writer.write(space + methodString + "\n")
                                    }
                                    return@write
                                }
                            }
                            writer.write(s + "\n")
                        }
                    }
                }
            }
            hudShaders.clear()
        }.onFailure { e ->
            warn("Unable to load shader.yml")
            warn("Reason: ${e.message}")
        }
    }

    override fun end() {
    }
}