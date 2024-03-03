package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.text.DecimalFormat

object ConfigManager: MythicHudManager {

    var barColor = BarColor.RED
        private set

    var info = EMPTY_COMPONENT
        private set
    var warn = EMPTY_COMPONENT
        private set
    var defaultHud = emptyList<String>()

    var numberFormat = DecimalFormat("#,###.#")

    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
        val file = File(DATA_FOLDER, "config.yml")
        if (!file.exists()) PLUGIN.saveResource("config.yml", false)
        runCatching {
            val yaml = file.toYaml()
            yaml.getString("info")?.let {
                info = it.deserializeToComponent()
            }
            yaml.getString("warn")?.let {
                warn = it.deserializeToComponent()
            }
            barColor = yaml.getString("bar-color")?.let {
                runCatching {
                    BarColor.valueOf(it.uppercase())
                }.getOrNull()
            } ?: BarColor.RED
            defaultHud = yaml.getStringList("default-hud")
            numberFormat = yaml.getString("number-format")?.let {
                runCatching {
                    DecimalFormat(it)
                }.getOrNull()
            } ?: DecimalFormat("#,###.#")
            PLUGIN.getResource("background.png")?.buffered()?.use { input ->
                resource.bossBar
                    .subFolder("sprites")
                    .subFolder("boss_bar")
                    .subFile("${barColor.name.lowercase()}_background.png")
                    .outputStream()
                    .buffered()
                    .use { output ->
                        input.copyTo(output)
                    }
            }
            PLUGIN.getResource("bars.png")?.buffered()?.use { target ->
                val oldImage = target.toImage()
                val yAxis = 10 * barColor.ordinal
                BufferedImage(oldImage.width, oldImage.height, BufferedImage.TYPE_INT_ARGB).apply {
                    createGraphics().run {
                        if (barColor.ordinal > 0) drawImage(oldImage.getSubimage(0, 0, oldImage.width, yAxis), 0, 0, null)
                        drawImage(oldImage.getSubimage(0, yAxis + 5, oldImage.width, oldImage.height - yAxis - 5), 0, yAxis + 5, null)
                        dispose()
                    }
                }.save(File(resource.bossBar, "bars.png"))
            }
        }.onFailure { e ->
            warn("Unable to load config.yml")
            warn("Reason: ${e.message}")
        }
    }

    override fun end() {
    }
}