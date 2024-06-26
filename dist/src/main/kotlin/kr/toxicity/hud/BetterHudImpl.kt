package kr.toxicity.hud

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.bedrock.BedrockAdapter
import kr.toxicity.hud.api.event.PluginReloadStartEvent
import kr.toxicity.hud.api.event.PluginReloadedEvent
import kr.toxicity.hud.api.manager.*
import kr.toxicity.hud.api.nms.NMS
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.plugin.ReloadResult
import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.bedrock.FloodgateAdapter
import kr.toxicity.hud.bedrock.GeyserAdapter
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.scheduler.FoliaScheduler
import kr.toxicity.hud.scheduler.StandardScheduler
import kr.toxicity.hud.util.*
import net.byteflux.libby.BukkitLibraryManager
import net.byteflux.libby.Library
import net.byteflux.libby.relocation.Relocation
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.jar.JarFile

@Suppress("UNUSED")
class BetterHudImpl: BetterHud() {

    private val isFolia = runCatching {
        Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler")
        true
    }.getOrDefault(false)
    private val isPaper = isFolia || runCatching {
        Class.forName("com.destroystokyo.paper.profile.PlayerProfile")
        true
    }.getOrDefault(false)
    init {
        if (!dataFolder.exists()) loadAssets("default", dataFolder.apply {
            mkdir()
        })
        BukkitLibraryManager(this, ".libraries").run {
            addMavenCentral()
            listOf(
                "adventure-api",
                "adventure-key",
                "adventure-text-logger-slf4j",
                "adventure-text-serializer-ansi",
                "adventure-text-serializer-gson",
                "adventure-text-serializer-plain",
                "adventure-text-serializer-legacy",
                "adventure-nbt",
                "adventure-text-serializer-json",
                "adventure-text-serializer-gson-legacy-impl",
                "adventure-text-serializer-json-legacy-impl",
            ).forEach {
                loadLibrary(Library.builder()
                    .groupId("net{}kyori")
                    .artifactId(it)
                    .version(ADVENTURE_VERSION)
                    .relocate(Relocation("net{}kyori", "hud{}net{}kyori"))
                    .build())
            }
            listOf(
                "examination-api",
                "examination-string"
            ).forEach {
                loadLibrary(Library.builder()
                    .groupId("net{}kyori")
                    .artifactId(it)
                    .version(EXAMINATION_VERSION)
                    .relocate(Relocation("net{}kyori", "hud{}net{}kyori"))
                    .build())
            }
            listOf(
                "adventure-platform-bukkit",
                "adventure-platform-api",
                "adventure-platform-facet",
            ).forEach {
                loadLibrary(Library.builder()
                    .groupId("net{}kyori")
                    .artifactId(it)
                    .version(PLATFORM_VERSION)
                    .relocate(Relocation("net{}kyori", "hud{}net{}kyori"))
                    .build())
            loadLibrary(Library.builder()
                .groupId("net{}kyori")
                .artifactId("option")
                .version("1.0.0")
                .relocate(Relocation("net{}kyori", "hud{}net{}kyori"))
                .build())
            }
        }
    }

    private val managers = listOf(
        ConfigManagerImpl,
        MinecraftManager,
        CommandManager,
        CompatibilityManager,
        ModuleManager,
        DatabaseManagerImpl,

        ListenerManagerImpl,
        PlaceholderManagerImpl,
        TriggerManagerImpl,

        BackgroundManager,
        ImageManager,
        TextManager,
        PlayerHeadManager,
        LayoutManager,
        HudManagerImpl,
        PopupManagerImpl,
        CompassManagerImpl,

        ShaderManagerImpl,
        PlayerManager
    )

    private lateinit var nms: NMS
    private lateinit var audience: BukkitAudiences
    private lateinit var bedrockAdapter: BedrockAdapter

    private val scheduler = if (isFolia) FoliaScheduler() else StandardScheduler()


    override fun onEnable() {
        val pluginManager = Bukkit.getPluginManager()

        nms = when (MinecraftVersion.current) {
            MinecraftVersion.version1_21 -> kr.toxicity.hud.nms.v1_21_R1.NMSImpl()
            MinecraftVersion.version1_20_5, MinecraftVersion.version1_20_6 -> kr.toxicity.hud.nms.v1_20_R4.NMSImpl()
            MinecraftVersion.version1_20_3, MinecraftVersion.version1_20_4 -> kr.toxicity.hud.nms.v1_20_R3.NMSImpl()
            MinecraftVersion.version1_20_2 -> kr.toxicity.hud.nms.v1_20_R2.NMSImpl()
            MinecraftVersion.version1_20, MinecraftVersion.version1_20_1 -> kr.toxicity.hud.nms.v1_20_R1.NMSImpl()
            MinecraftVersion.version1_19_4 -> kr.toxicity.hud.nms.v1_19_R3.NMSImpl()
            MinecraftVersion.version1_19_2, MinecraftVersion.version1_19_3 -> kr.toxicity.hud.nms.v1_19_R2.NMSImpl()
            MinecraftVersion.version1_19, MinecraftVersion.version1_19_1 -> kr.toxicity.hud.nms.v1_19_R1.NMSImpl()
            MinecraftVersion.version1_18_2 -> kr.toxicity.hud.nms.v1_18_R2.NMSImpl()
            MinecraftVersion.version1_18, MinecraftVersion.version1_18_1 -> kr.toxicity.hud.nms.v1_18_R1.NMSImpl()
            MinecraftVersion.version1_17, MinecraftVersion.version1_17_1 -> kr.toxicity.hud.nms.v1_17_R1.NMSImpl()
            else -> {
                warn("Unsupported minecraft version: ${MinecraftVersion.current}")
                pluginManager.disablePlugin(this)
                return
            }
        }
        bedrockAdapter = if (pluginManager.isPluginEnabled("Geyser-Spigot")) {
            GeyserAdapter()
        } else if (pluginManager.isPluginEnabled("floodgate")) {
            FloodgateAdapter()
        } else BedrockAdapter { false }
        audience = BukkitAudiences.create(this)

        managers.forEach {
            it.start()
        }
        Bukkit.getOnlinePlayers().forEach {
            PlayerManager.register(it)
        }
        task {
            CompletableFuture.runAsync {
                reload()
                info(
                    "Minecraft version: ${MinecraftVersion.current}, NMS version: ${nms.version}",
                    "Plugin enabled."
                )
            }
        }
        runWithExceptionHandling(Bukkit.getConsoleSender().audience, "Unable to get latest version.") {
            HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=115559/"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()
            ).thenAccept {
                val body = it.body()
                if (description.version != body) {
                    warn("New version found: $body")
                    warn("Download: https://www.spigotmc.org/resources/115559")
                    Bukkit.getPluginManager().registerEvents(object : Listener {
                        @EventHandler
                        fun join(e: PlayerJoinEvent) {
                            val player = e.player
                            if (player.isOp && ConfigManagerImpl.versionCheck) {
                                player.info("New BetterHud version found: $body")
                                player.info(Component.text("Download: https://www.spigotmc.org/resources/115559")
                                    .clickEvent(ClickEvent.clickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://www.spigotmc.org/resources/115559"
                                    )))
                            }
                        }
                    }, this)
                }
            }
        }
    }

    @Volatile
    private var onReload = false

    override fun reload(sender: Audience): ReloadResult {
        synchronized(this) {
            if (onReload) {
                return ReloadResult(ReloadState.STILL_ON_RELOAD, 0)
            }
            onReload = true
        }
        val time = System.currentTimeMillis()
        val result = CompletableFuture.supplyAsync {
            PluginReloadStartEvent().call()
            val result = runWithExceptionHandling(sender, "Unable to reload.") {
                managers.forEach {
                    it.preReload()
                }
                val resource = GlobalResource()
                managers.forEach {
                    CompletableFuture.runAsync {
                        it.reload(sender, resource)
                    }.join()
                }
                managers.forEach {
                    it.postReload()
                }
                PackGenerator.generate(sender)
                ReloadResult(ReloadState.SUCCESS, System.currentTimeMillis() - time)
            }.getOrElse {
                ReloadResult(ReloadState.FAIL, System.currentTimeMillis() - time)
            }
            PluginReloadedEvent(result).call()
            result
        }.join()
        synchronized(this) {
            onReload = false
        }
        return result
    }


    override fun onDisable() {
        audience.close()
        managers.forEach {
            it.end()
        }
        DatabaseManagerImpl.currentDatabase.close()
        info("Plugin disabled.")
    }

    override fun getNMS(): NMS = nms
    override fun getWidth(codepoint: Int): Int = TextManager.getWidth(codepoint)
    override fun getBedrockAdapter(): BedrockAdapter = bedrockAdapter
    override fun getAudiences(): BukkitAudiences = audience
    override fun getHudPlayer(player: Player): HudPlayer = PlayerManager.getHudPlayer(player)


    override fun loadAssets(prefix: String, dir: File) {
        loadAssets(prefix) { s, i ->
            File(dir, s).apply {
                parentFile.mkdirs()
            }.outputStream().buffered().use {
                i.copyTo(it)
            }
        }
    }

    override fun loadAssets(prefix: String, consumer: BiConsumer<String, InputStream>) {
        loadAssets(prefix) { s, i ->
            consumer.accept(s, i)
        }
    }

    private fun loadAssets(prefix: String, consumer: (String, InputStream) -> Unit) {
        JarFile(file).use {
            it.entries().asSequence().forEach { entry ->
                if (!entry.name.startsWith(prefix)) return@forEach
                if (entry.name.length <= prefix.length + 1) return@forEach
                val name = entry.name.substring(prefix.length + 1)
                if (!entry.isDirectory) getResource(entry.name)?.buffered()?.use { stream ->
                    consumer(name, stream)
                }
            }
        }
    }

    override fun getEncodedNamespace(): String = NAME_SPACE_ENCODED
    override fun getScheduler(): HudScheduler = scheduler
    override fun isPaper(): Boolean = isPaper
    override fun isFolia(): Boolean = isFolia
    override fun isMergeBossBar(): Boolean = ConfigManagerImpl.mergeBossBar
    override fun getPlaceholderManager(): PlaceholderManager = PlaceholderManagerImpl
    override fun getListenerManager(): ListenerManager = ListenerManagerImpl
    override fun getPopupManager(): PopupManager = PopupManagerImpl
    override fun getTriggerManager(): TriggerManager = TriggerManagerImpl
    override fun getHudManager(): HudManager = HudManagerImpl
    override fun getDatabaseManager(): DatabaseManager = DatabaseManagerImpl
    override fun getShaderManager(): ShaderManager = ShaderManagerImpl
    override fun getCompassManager(): CompassManager = CompassManagerImpl
    override fun getConfigManager(): ConfigManager = ConfigManagerImpl
    override fun isOnReload(): Boolean = onReload
    override fun getDefaultKey(): Key = DEFAULT_KEY
    override fun translate(locale: String, key: String): String? = TextManager.translate(locale, key)
}