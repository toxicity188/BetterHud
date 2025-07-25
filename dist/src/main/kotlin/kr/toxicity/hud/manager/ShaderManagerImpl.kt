package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.ShaderManager
import kr.toxicity.hud.api.manager.ShaderManager.*
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.pack.PackOverlay
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.HotBarShader
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.util.*
import net.kyori.adventure.bossbar.BossBar
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.collections.plus

object ShaderManagerImpl : BetterHudManager, ShaderManager {

    override val managerName: String = "Shader"
    override val supportExternalPacks: Boolean = false

    var barColor = BossBar.Color.YELLOW
        private set

    private val tagPattern = Pattern.compile("#(?<name>[a-zA-Z]+)")
    private val tagBuilders: MutableMap<String, () -> List<String>> = mutableMapOf(
        "CreateConstant" to {
            constants.map {
                "#define ${it.key} ${it.value}"
            }
        },
        "CreateLayout" to {
            compiledLayout
        },
    )

    private val hudShaders = TreeMap<HudShader, MutableList<(Int) -> Unit>>()
    private var compiledLayout = arrayListOf<String>()

    private val tagSupplierMap = ConcurrentHashMap<ShaderType, ShaderTagSupplier>()


    @Synchronized
    fun addHudShader(shader: HudShader, consumer: (Int) -> Unit) {
        synchronized(hudShaders) {
            hudShaders.computeIfAbsent(shader) {
                ArrayList()
            }.add(consumer)
        }
    }

    override fun addTagSupplier(type: ShaderType, supplier: ShaderTagSupplier) {
        tagSupplierMap[type] = tagSupplierMap[type]?.let {
            it + supplier
        } ?: supplier
    }

    private val constants = mutableMapOf<String, String>()
    private var replaceSet = mutableSetOf<String>()

    private val shaderConstants = mutableMapOf(
        "HEIGHT_BIT" to HUD_DEFAULT_BIT.toString(),
        "MAX_BIT" to HUD_MAX_BIT.toString(),
        "ADD_OFFSET" to HUD_ADD_HEIGHT.toString()
    )

    override fun addConstant(key: String, value: String) {
        shaderConstants[key] = value
    }

    override fun start() {
        ShaderType.entries.forEach {
            addTagSupplier(it, EMPTY_SUPPLIER)
        }
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        constants.clear()
        replaceSet = mutableSetOf()
        constants += shaderConstants
        constants["DEFAULT_OFFSET"] = "${10 + 17 * (ConfigManagerImpl.bossbarResourcePackLine - 1)}"
        runCatching {
            val yaml = PluginConfiguration.SHADER.create()
            barColor = yaml["bar-color"]?.asString()?.let {
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
            if (yaml.getAsBoolean("disable-level-text", false)) replaceSet += "HideExp"
            yaml["hotbar"]?.asObject()?.let {
                if (it.getAsBoolean("enable-hotbar-relocation", false)) {
                    replaceSet += "RemapHotBar"
                    val locations =
                        it.get("locations")?.asObject().ifNull { "locations configuration not set." }
                    (1..10).map { index ->
                        locations.get(index.toString())?.asObject()?.let { shaderConfig ->
                            HotBarShader(
                                shaderConfig["gui"]?.asObject()?.let { gui ->
                                    gui.getAsDouble("x", 0.0) to gui.getAsDouble("y", 0.0)
                                } ?: (0.0 to 0.0),
                                shaderConfig["pixel"]?.asObject()?.let { pixel ->
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
            compileShader(resource)
        }.handleFailure(info) {
            "Unable to load shader.yml"
        }
    }

    private fun compileShader(resource: GlobalResource) {
        compiledLayout = hudShaders.entries.foldIndexed(arrayListOf()) { index, arr, entry ->
            val shader = entry.key
            val id = index + 1
            arr.add("case ${id}:")
            if (shader.property > 0) arr.add("    property = ${shader.property};")
            if (shader.opacity < 1.0) arr.add("    opacity = ${shader.opacity.toFloat()};")
            val static = shader.renderScale.scale.staticScale
            fun applyScale(offset: Int, scale: Double, pos: String) {
                if (scale != 1.0 || static) {
                    val scaleFloat = scale.toFloat()
                    arr.add("    pos.$pos = (pos.$pos - (${offset})) * ${if (static) "$scaleFloat * uiScreen.$pos" else scaleFloat} + (${offset});")
                }
            }
            applyScale(shader.renderScale.relativeOffset.x, shader.renderScale.scale.x, "x")
            applyScale(shader.renderScale.relativeOffset.y, shader.renderScale.scale.y, "y")
            if (shader.gui.x != 0.0) arr.add("    xGui = ui.x * ${shader.gui.x.toFloat()} / 100.0;")
            if (shader.gui.y != 0.0) arr.add("    yGui = ui.y * ${shader.gui.y.toFloat()} / 100.0;")
            if (shader.layer != 0) arr.add("    layer = ${shader.layer};")
            if (shader.outline != 0) arr.add("    outline = true;")
            arr.add("    break;")
            entry.value.forEach {
                it(id)
            }
            arr
        }
        for (overlay in PackOverlay.entries) {
            loadShaders(overlay).forEach { (key, byte) ->
                val path = resource.core + key
                PackGenerator.addTask(if (overlay.ordinal == 0) path else listOf(overlay.overlayName) + path) {
                    byte
                }
            }
        }
        compiledLayout = arrayListOf()
        hudShaders.clear()
    }

    private fun loadShaders(overlay: PackOverlay): List<Pair<String, ByteArray>> {
        constants["SHADER_VERSION"] = overlay.ordinal.toString()
        val shaders = ShaderType.entries.map {
            it to it.lines()
        }
        return shaders.map { (key, args) ->
            val tagSupplier = (tagSupplierMap[key] ?: EMPTY_SUPPLIER).get()
            key.shadersCoreName to buildString {
                args.forEach write@ { string ->
                    var s = string
                    if (s.startsWith("//")) {
                        val get = s.substringBefore(' ')
                        if (replaceSet.contains(get.substring(2))) s = s.substring(get.length)
                    }
                    if (s.isEmpty()) return@write
                    val tagMatcher = tagPattern.matcher(s)
                    if (tagMatcher.find()) {
                        val group = tagMatcher.group("name")
                        (tagBuilders[group]?.invoke() ?: tagSupplier[group])?.let {
                            it.forEach apply@ { methodString ->
                                if (methodString.isEmpty() || methodString.startsWith("//")) return@apply
                                val appendEnter = methodString.first() == '#'
                                if (appendEnter && (isEmpty() || last() != '\n')) append('\n')
                                append(methodString.replace("  ", ""))
                                if (appendEnter) append('\n')
                            }
                            return@write
                        }
                    }
                    var tr = s.trim()
                    if (tr.isNotEmpty()) {
                        if (isNotEmpty() && tr.first() == '#') {
                            if (last() != '\n') tr = '\n' + tr
                            tr += '\n'
                        }
                        append(tr.substringBeforeLast("//"))
                    }
                }
            }.toByteArray()
        }
    }

    override fun end() {
    }
}
