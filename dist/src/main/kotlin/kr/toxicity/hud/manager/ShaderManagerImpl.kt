package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.ShaderManager
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.hud.HudImpl
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HotBarShader
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

object ShaderManagerImpl: BetterHudManager, ShaderManager {
    var barColor = BossBar.Color.YELLOW
        private set

    private val tagPattern = Pattern.compile("#(?<name>[a-zA-Z]+)")
    private val deactivatePattern = Pattern.compile("//(?<name>[a-zA-Z]+)")
    private val tagBuilders: MutableMap<String, () -> List<String>> = mutableMapOf(
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
                        if (shader.opacity < 1.0) add("    opacity = ${shader.opacity.toFloat()};")
                        if (shader.renderScale.scale.x != 1.0) add("    pos.x = (pos.x - (${shader.renderScale.relativeOffset.x})) * ${shader.renderScale.scale.x} + (${shader.renderScale.relativeOffset.x});")
                        if (shader.renderScale.scale.y != 1.0) add("    pos.y = (pos.y - (${shader.renderScale.relativeOffset.y})) * ${shader.renderScale.scale.y} + (${shader.renderScale.relativeOffset.y});")
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
    )

    private val hudShaders = TreeMap<HudShader, MutableList<(Int) -> Unit>>()


    fun addTagBuilder(key: String, valueBuilder: () -> List<String>) {
        tagBuilders[key] = valueBuilder
    }

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
                        return name to run {
                            val f = File(DATA_FOLDER, name)
                            if (!f.exists()) BOOTSTRAP.resource(name).ifNull("Unknown resource: $name").buffered().use {
                                f.outputStream().buffered().use { output ->
                                    output.write(it.readAllBytes())
                                }
                            }
                            runCatching {
                                f.bufferedReader(Charsets.UTF_8)
                            }.getOrNull() ?: throw RuntimeException("plugin jar file has a problem.")
                        }
                    }

                    val shaders = listOf(
                        getReader("rendertype_entity_cutout.vsh"),
                        getReader("rendertype_entity_translucent_cull.vsh"),
                        getReader("rendertype_text.vsh")
                    )
                    constants += shaderConstants
                    constants["DEFAULT_OFFSET"] = "${10 + 17 * (ConfigManagerImpl.bossbarLine - 1)}"
                    val replaceList = mutableSetOf<String>()

                    val yaml = PluginConfiguration.SHADER.create()
                    barColor = yaml.get("bar-color")?.asString()?.let {
                        runCatching {
                            BossBar.Color.valueOf(it.uppercase())
                        }.getOrNull()
                    } ?: BossBar.Color.RED
                    fun copy(suffix: String) {
                        BOOTSTRAP.resource("background.png")?.buffered()?.use { input ->
                            val byte = input.readAllBytes()
                            PackGenerator.addTask(resource.bossBar + listOf("sprites", "boss_bar", "${barColor.name.lowercase()}_$suffix.png")) {
                                byte
                            }
                        }
                    }
                    copy("background")
                    copy("progress")
                    BOOTSTRAP.resource("bars.png")?.buffered()?.use { target ->
                        val oldImage = target.toImage()
                        val yAxis = 10 * barColor.ordinal
                        PackGenerator.addTask(resource.bossBar + "bars.png") {
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

                    if (yaml.getAsBoolean("disable-level-text", false)) replaceList.add("HideExp")
                    if (yaml.getAsBoolean("disable-item-name", false)) replaceList.add("HideItemName")

                    yaml.get("hotbar")?.asObject()?.let {
                        if (it.getAsBoolean("disable", false)) {
                            replaceList.add("RemapHotBar")
                            val locations =
                                it.get("locations")?.asObject().ifNull("locations configuration not set.")
                            (1..10).map { index ->
                                locations.get(index.toString())?.asObject()?.let { shaderConfig ->
                                    HotBarShader(
                                        shaderConfig.get("gui")?.asObject()?.let { gui ->
                                            gui.getAsDouble("x", 0.0) to gui.getAsDouble("y", 0.0)
                                        } ?: (0.0 to 0.0),
                                        shaderConfig.get("pixel")?.asObject()?.let { pixel ->
                                            pixel.getAsInt("x", 0) to pixel.getAsInt("y", 0)
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
                            PackGenerator.addTask(resource.core + shader.first) {
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