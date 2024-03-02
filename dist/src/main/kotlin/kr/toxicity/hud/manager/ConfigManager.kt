package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.boss.BarColor
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
                resource.bossBar.subFile("${barColor.name.lowercase()}_background.png").outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
        }.onFailure { e ->
            warn("Unable to load config.yml")
            warn("Reason: ${e.message}")
        }
    }

    override fun end() {
    }
}