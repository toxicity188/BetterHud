package kr.toxicity.hud.manager

import kr.toxicity.hud.pack.PackType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.io.File
import java.text.DecimalFormat

object ConfigManager: BetterHudManager {

    val info = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.GOLD))
    val warn = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.RED))
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
    var buildFolderLocation = "BetterHud/build"
        private set
    var enableProtection = true
        private set

    var mergeBossBar = true
        private set
    var packType = PackType.FOLDER
        private set
    var enableSelfHost = false
        private set
    var selfHostPort = 8163
        private set
    var mergeOtherFolders = emptyList<String>()
        private set

    override fun start() {

    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        callback()
    }

    override fun preReload() {
        val file = File(DATA_FOLDER, "config.yml")
        if (!file.exists()) PLUGIN.saveResource("config.yml", false)
        runCatching {
            val yaml = file.toYaml()
            defaultHud = yaml.getStringList("default-hud")
            defaultPopup = yaml.getStringList("default-popup")
            yaml.getString("default-font-name")?.let {
                defaultFontName = it
            }
            yaml.getString("pack-type")?.let {
                runCatching {
                    packType = PackType.valueOf(it.uppercase())
                }
            }
            tickSpeed = yaml.getLong("tick-speed", 1)
            numberFormat = (yaml.getString("number-format")?.let {
                runCatching {
                    DecimalFormat(it)
                }.getOrNull()
            } ?: DecimalFormat("#,###.#"))
            disableToBedrockPlayer = yaml.getBoolean("disable-to-bedrock-player", true)
            yaml.getString("build-folder-location")?.let {
                buildFolderLocation = it
            }
            enableProtection = yaml.getBoolean("enable-protection")
            mergeBossBar = yaml.getBoolean("merge-boss-bar", true)
            enableSelfHost = yaml.getBoolean("enable-self-host")
            mergeOtherFolders = yaml.getStringList("merge-other-folders")
            selfHostPort = yaml.getInt("self-host-port", 8163)
        }.onFailure { e ->
            warn("Unable to load config.yml")
            warn("Reason: ${e.message}")
        }
    }
    override fun end() {
    }
}