package kr.toxicity.hud.manager

import kr.toxicity.hud.api.event.CreateShaderEvent
import kr.toxicity.hud.api.manager.ShaderManager
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HotBarShader
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import org.bukkit.boss.BarColor
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.TreeMap
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

object ShaderManagerImpl: BetterHudManager, ShaderManager {
    var barColor = BarColor.YELLOW
        private set

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
                hudShaders.entries.forEachIndexed { index, entry ->
                    addAll(ArrayList<String>().apply {
                        val shader = entry.key
                        val id = index + 1
                        add("case ${id}:")
                        if (shader.gui.x != 0.0) add("    xGui = ui.x * ${shader.gui.x.toFloat()} / 100.0;")
                        if (shader.gui.y != 0.0) add("    yGui = ui.y * ${shader.gui.y.toFloat()} / 100.0;")
                        if (shader.layer != 0) add("    layer = ${shader.layer};")
                        if (shader.outline) add("    outline = true;")
                        add("    break;")
                        entry.value.forEach {
                            it(id)
                        }
                    })
                }
                hudShaders.clear()
            }
        },
        "CreateOtherShader" to {
            CreateShaderEvent().apply {
                call()
            }.lines
        }
    )

    private val hudShaders = TreeMap<HudShader, MutableList<(Int) -> Unit>>()



    @Synchronized
    fun addHudShader(shader: HudShader, consumer: (Int) -> Unit) {
        synchronized(hudShaders) {
            hudShaders.computeIfAbsent(shader) {
                ArrayList()
            }.add(consumer)
        }
    }

    private val constants = mutableMapOf<String, String>()

    private val shaderConstants = mutableMapOf(
        "HEIGHT_BIT" to HudImpl.DEFAULT_BIT.toString(),
        "MAX_BIT" to HudImpl.MAX_BIT.toString(),
        "ADD_OFFSET" to HudImpl.ADD_HEIGHT.toString()
    )

    override fun addConstant(key: String, value: String) {
        shaderConstants[key] = value
    }

    override fun start() {

    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        CompletableFuture.runAsync {
            synchronized(this) {
                constants.clear()
                runWithExceptionHandling(sender, "Unable to load shader.yml") {
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
                    constants["DEFAULT_OFFSET"] = "${64 + 17 * (ConfigManagerImpl.bossbarLine - 1)}"
                    val replaceList = mutableSetOf<String>()

                    val yaml = PluginConfiguration.SHADER.create()
                    barColor = yaml.getString("bar-color")?.let {
                        runCatching {
                            BarColor.valueOf(it.uppercase())
                        }.getOrNull()
                    } ?: BarColor.RED
                    fun copy(suffix: String) {
                        PLUGIN.getResource("background.png")?.buffered()?.use { input ->
                            val byte = input.readAllBytes()
                            PackGenerator.addTask(ArrayList(resource.bossBar).apply {
                                add("sprites")
                                add("boss_bar")
                                add("${barColor.name.lowercase()}_$suffix.png")
                            }) {
                                byte
                            }
                        }
                    }
                    copy("background")
                    copy("progress")
                    PLUGIN.getResource("bars.png")?.buffered()?.use { target ->
                        val oldImage = target.toImage()
                        val yAxis = 10 * barColor.ordinal
                        PackGenerator.addTask(ArrayList(resource.bossBar).apply {
                            add("bars.png")
                        }) {
                            BufferedImage(oldImage.width, oldImage.height, BufferedImage.TYPE_INT_ARGB).apply {
                                createGraphics().run {
                                    if (barColor.ordinal > 0) drawImage(
                                        oldImage.getSubimage(
                                            0,
                                            0,
                                            oldImage.width,
                                            yAxis
                                        ), 0, 0, null
                                    )
                                    drawImage(
                                        oldImage.getSubimage(
                                            0,
                                            yAxis + 10,
                                            oldImage.width,
                                            oldImage.height - yAxis - 10
                                        ), 0, yAxis + 10, null
                                    )
                                    dispose()
                                }
                            }.toByteArray()
                        }
                    }

                    if (yaml.getBoolean("disable-level-text")) replaceList.add("HideExp")
                    if (yaml.getBoolean("disable-item-name")) replaceList.add("HideItemName")

                    yaml.getConfigurationSection("hotbar")?.let {
                        if (it.getBoolean("disable")) {
                            replaceList.add("RemapHotBar")
                            val locations =
                                it.getConfigurationSection("locations").ifNull("locations configuration not set.")
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
                            val byte = ByteArrayOutputStream().use { writer ->
                                reader.readLines().forEach write@{ string ->
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
                                                writer.write((space + methodString + "\n").toByteArray())
                                            }
                                            return@write
                                        }
                                    }
                                    writer.write((s + "\n").toByteArray())
                                }
                                writer
                            }.toByteArray()
                            PackGenerator.addTask(ArrayList(resource.core).apply {
                                add(shader.first)
                            }) {
                                byte
                            }
                        }
                    }
                }
            }
        }.join()
    }

    override fun end() {
    }
}