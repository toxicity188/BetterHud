package kr.toxicity.hud.manager

import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import java.io.File
import java.text.DecimalFormat

object ConfigManager: BetterHudManager {

    var info = EMPTY_COMPONENT
        private set
    var warn = EMPTY_COMPONENT
        private set
    var defaultHud = emptyList<String>()
        private set
    var defaultPopup = emptyList<String>()
        private set

    var numberFormat = DecimalFormat("#,###.#")
        private set
    var defaultFontName = "font.ttf"
        private set
    var tickSpeed = 1L
        private set
    var disableToBedrockPlayer = true
        private set
    var buildFolderLocation = "BetterHud\\build"
        private set
    var separateResourcePackNameSpace = false
        private set

    override fun start() {

    }

    override fun reload(resource: GlobalResource) {
    }

    override fun preReload() {
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
            yaml.getString("default-font-name")?.let {
                defaultFontName = it
            }
            tickSpeed = yaml.getLong("tick-speed", 1)
            numberFormat = (yaml.getString("number-format")?.let {
                runCatching {
                    DecimalFormat(it)
                }.getOrNull()
            } ?: DecimalFormat("#,###.#")).apply {
                maximumFractionDigits = 0
            }
            disableToBedrockPlayer = yaml.getBoolean("disable-to-bedrock-player", true)
            separateResourcePackNameSpace = yaml.getBoolean("separate-resource-pack-namespace", false)
            yaml.getString("build-folder-location")?.let {
                buildFolderLocation = it
            }
        }.onFailure { e ->
            warn("Unable to load config.yml")
            warn("Reason: ${e.message}")
        }
    }
    override fun end() {
    }
}