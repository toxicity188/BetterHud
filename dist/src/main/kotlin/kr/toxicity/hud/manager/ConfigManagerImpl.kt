package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.ConfigManager
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.pack.PackType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.resource.KeyResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bstats.bukkit.Metrics
import java.io.File
import java.text.DecimalFormat

object ConfigManagerImpl: BetterHudManager, ConfigManager {
    var key = KeyResource(NAME_SPACE)
        private set

    val info = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.GOLD))
    val warn = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.RED))
    private var line = 1
    var defaultHud = emptyList<String>()
        private set
    var defaultPopup = emptyList<String>()
        private set
    var defaultCompass = emptyList<String>()
        private set

    var numberFormat = DecimalFormat("#,###.#")
        private set
    var defaultFontName = "font.ttf"
        private set
    var tickSpeed = 1L
        private set
    var disableToBedrockPlayer = true
        private set
    var buildFolderLocation = "BetterHud/build".replace('/', File.separatorChar)
        private set
    var enableProtection = true
        private set
    var forceUpdate = false
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

    var needToUpdatePack = false
        private set
    var loadingHead = "random"
        private set

    private var metrics: Metrics? = null

    override fun start() {

    }

    override fun getBossbarLine(): Int = line
    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        callback()
    }

    override fun preReload() {
        runCatching {
            needToUpdatePack = false
            val yaml = PluginConfiguration.CONFIG.create()
            defaultHud = yaml.getStringList("default-hud")
            defaultPopup = yaml.getStringList("default-popup")
            defaultCompass = yaml.getStringList("default-compass")
            yaml.getString("default-font-name")?.let {
                if (defaultFontName != it) needToUpdatePack = true
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
                buildFolderLocation = it.replace('/', File.separatorChar)
            }
            yaml.getString("namespace")?.let {
                key = KeyResource(it.lowercase())
            }
            val newLine = yaml.getInt("bossbar-line", 1).coerceAtLeast(1).coerceAtMost(7)
            if (line != newLine) {
                line = newLine
                needToUpdatePack = true
            }
            enableProtection = yaml.getBoolean("enable-protection")
            mergeBossBar = yaml.getBoolean("merge-boss-bar", true)
            enableSelfHost = yaml.getBoolean("enable-self-host")
            mergeOtherFolders = yaml.getStringList("merge-other-folders")
            selfHostPort = yaml.getInt("self-host-port", 8163)
            forceUpdate = yaml.getBoolean("force-update")
            if (yaml.getBoolean("metrics") && metrics == null) {
                metrics = Metrics(PLUGIN, 21287)
            } else {
                metrics?.shutdown()
                metrics = null
            }
            yaml.getString("loading-head")?.let {
                loadingHead = it
            }
        }.onFailure { e ->
            warn(
                "Unable to load config.yml",
                "Reason: ${e.message}"
            )
        }
    }
    override fun end() {
    }
}