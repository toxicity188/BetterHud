package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.ConfigManager
import kr.toxicity.hud.configuration.PluginConfiguration
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.pack.PackType
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.resource.KeyResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.io.File
import java.text.DecimalFormat

object ConfigManagerImpl : BetterHudManager, ConfigManager {
    var key = KeyResource(NAME_SPACE)
        private set

    val info = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.GOLD))
    val warn = EMPTY_COMPONENT.append(Component.text("[!] ").color(NamedTextColor.RED))
    private var line = 1

    var bossbarResourcePackLine = line
        private set

    var defaultHud = emptyList<String>()
        private set
    var defaultPopup = emptyList<String>()
        private set
    var defaultCompass = emptyList<String>()
        private set
    var versionCheck = true
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
    var selfHostIp = "*"
        private set
    var selfHostPort = 8163
        private set
    var mergeOtherFolders = emptyList<String>()
        private set

    var needToUpdatePack = false
        private set
    var loadingHead = "random"
        private set
    var debug = false
        private set

    var resourcePackObfuscation = false
        private set

    var clearBuildFolder = true
        private set

    var minecraftJarVersion = "bukkit"
        private set

    var loadMinecraftDefaultTextures = true
        private set
    var includedMinecraftTextures = listOf(
        "block",
        "item"
    )
        private set

    var useLegacyFormat = true
    var legacySerializer = LEGACY_AMPERSAND
        private set
    private var removeDefaultHotbar = false
    var disableLegacyOffset = false
        private set

    override fun start() {
    }

    override fun getBossbarLine(): Int = line
    override fun reload(sender: Audience, resource: GlobalResource) {
        if (removeDefaultHotbar) {
            PLUGIN.loadAssets("empty") { n, i ->
                val read = i.readAllBytes()
                PackGenerator.addTask(n.split('/')) {
                    read
                }
            }
        }
        PLUGIN.loadAssets("pack") { n, i ->
            val read = i.readAllBytes()
            PackGenerator.addTask(n.split('/')) {
                read
            }
        }
    }

    override fun preReload() {
        runCatching {
            File(DATA_FOLDER, "version.txt").bufferedWriter().use {
                it.write(BOOTSTRAP.version())
            }
            needToUpdatePack = false
            val yaml = PluginConfiguration.CONFIG.create()
            debug = yaml.getAsBoolean("debug", false)
            defaultHud = yaml["default-hud"]?.asArray()?.map {
                it.asString()
            } ?: emptyList()
            defaultPopup = yaml["default-popup"]?.asArray()?.map {
                it.asString()
            } ?: emptyList()
            defaultCompass = yaml["default-compass"]?.asArray()?.map {
                it.asString()
            } ?: emptyList()
            yaml["default-font-name"]?.asString()?.let {
                if (defaultFontName != it) needToUpdatePack = true
                defaultFontName = it
            }
            yaml["pack-type"]?.asString()?.let {
                runWithExceptionHandling(CONSOLE, "Unable to find this pack type: $it") {
                    packType = PackType.valueOf(it.uppercase())
                }
            }
            tickSpeed = yaml.getAsLong("tick-speed", 1)
            numberFormat = yaml["number-format"]?.asString()?.let {
                runWithExceptionHandling(CONSOLE, "Unable to read this number-format: $it") {
                    DecimalFormat(it)
                }.getOrNull()
            } ?: DecimalFormat("#,###.#")
            disableToBedrockPlayer = yaml.getAsBoolean("disable-to-bedrock-player", true)
            yaml["build-folder-location"]?.asString()?.let {
                buildFolderLocation = it.replace('/', File.separatorChar)
            }
            line = yaml.getAsInt("bossbar-line", 1).coerceAtLeast(1).coerceAtMost(7)
            var newLine = yaml.getAsInt("bossbar-resource-pack-line", 0)
            if (newLine < 1) {
                newLine = line
            }
            if (bossbarResourcePackLine != newLine) {
                bossbarResourcePackLine = newLine
                needToUpdatePack = true
            }
            versionCheck = yaml.getAsBoolean("version-check", false)
            enableProtection = yaml.getAsBoolean("enable-protection", false)
            mergeBossBar = yaml.getAsBoolean("merge-boss-bar", true)
            enableSelfHost = yaml.getAsBoolean("enable-self-host", false)
            mergeOtherFolders = yaml["merge-other-folders"]?.asArray()?.map {
                it.asString()
            } ?: emptyList()
            yaml["self-host-ip"]?.asString()?.let { ip ->
                selfHostIp = ip
            }
            selfHostPort = yaml.getAsInt("self-host-port", 8163)
            forceUpdate = yaml.getAsBoolean("force-update", false)
            resourcePackObfuscation = yaml.getAsBoolean("resourcepack-obfuscation", false)
            if (yaml.getAsBoolean("metrics", false)) {
                BOOTSTRAP.startMetrics()
            } else {
                BOOTSTRAP.endMetrics()
            }
            yaml["loading-head"]?.asString()?.let {
                loadingHead = it
            }
            clearBuildFolder = yaml.getAsBoolean("clear-build-folder", true)
            loadMinecraftDefaultTextures = yaml.getAsBoolean("load-minecraft-default-textures", true)
            includedMinecraftTextures = yaml["included-minecraft-list"]?.asArray()?.map {
                it.asString()
            } ?: emptyList()
            useLegacyFormat = yaml.getAsBoolean("use-legacy-format", true)
            yaml["legacy-serializer"]?.asString()?.let {
                runWithExceptionHandling(CONSOLE, "Unable to find legacy serializer.") {
                    legacySerializer = it.toLegacySerializer()
                }
            }
            key = KeyResource(yaml["namespace"]?.asString() ?: NAME_SPACE)
            minecraftJarVersion = yaml["minecraft-jar-version"]?.asString() ?: "bukkit"
            removeDefaultHotbar = yaml.getAsBoolean("remove-default-hotbar", false)
            disableLegacyOffset = yaml.getAsBoolean("disable-legacy-offset", false)
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