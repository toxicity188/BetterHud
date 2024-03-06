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

    var info = EMPTY_COMPONENT
        private set
    var warn = EMPTY_COMPONENT
        private set
    var defaultHud = emptyList<String>()
    var defaultPopup = emptyList<String>()

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
            defaultHud = yaml.getStringList("default-hud")
            defaultPopup = yaml.getStringList("default-popup")
            numberFormat = yaml.getString("number-format")?.let {
                runCatching {
                    DecimalFormat(it)
                }.getOrNull()
            } ?: DecimalFormat("#,###.#")
        }.onFailure { e ->
            warn("Unable to load config.yml")
            warn("Reason: ${e.message}")
        }
    }

    override fun end() {
    }
}