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
import kr.toxicity.hud.skript.SkriptManager
import kr.toxicity.hud.util.*
import net.byteflux.libby.BukkitLibraryManager
import net.byteflux.libby.Library
import net.byteflux.libby.relocation.Relocation
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
import java.util.function.Consumer
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
        CommandManager,
        CompatibilityManager,
        SkriptManager,
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

        ShaderManager,
        PlayerManager
    )

    private lateinit var nms: NMS
    private lateinit var audience: BukkitAudiences
    private lateinit var bedrockAdapter: BedrockAdapter

    private val scheduler = if (isFolia) FoliaScheduler() else StandardScheduler()


    override fun onEnable() {
        runCatching {
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
                            if (player.isOp) {
                                player.info("New BetterHud version found: $body")
                                player.info(Component.text("Download: https://www.spigotmc.org/resources/115559")
                                    .clickEvent(ClickEvent.clickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://www.spigotmc.org/resources/115559"
                                    )))
                            }
                        }
                    }, PLUGIN)
                }
            }
        }
        val pluginManager = Bukkit.getPluginManager()
        nms = when (val version = Bukkit.getServer().javaClass.`package`.name.split('.')[3]) {
            "v1_17_R1" -> kr.toxicity.hud.nms.v1_17_R1.NMSImpl()
            "v1_18_R1" -> kr.toxicity.hud.nms.v1_18_R1.NMSImpl()
            "v1_18_R2" -> kr.toxicity.hud.nms.v1_18_R2.NMSImpl()
            "v1_19_R1" -> kr.toxicity.hud.nms.v1_19_R1.NMSImpl()
            "v1_19_R2" -> kr.toxicity.hud.nms.v1_19_R2.NMSImpl()
            "v1_19_R3" -> kr.toxicity.hud.nms.v1_19_R3.NMSImpl()
            "v1_20_R1" -> kr.toxicity.hud.nms.v1_20_R1.NMSImpl()
            "v1_20_R2" -> kr.toxicity.hud.nms.v1_20_R2.NMSImpl()
            "v1_20_R3" -> kr.toxicity.hud.nms.v1_20_R3.NMSImpl()
            "v1_20_R4" -> kr.toxicity.hud.nms.v1_20_R4.NMSImpl()
            else -> {
                warn("Unsupported bukkit version: $version")
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
        reload {
            info("Plugin enabled.")
        }
    }

    @Volatile
    private var onReload = false

    override fun reload(consumer: Consumer<ReloadResult>) {
        if (onReload) {
            consumer.accept(ReloadResult(ReloadState.STILL_ON_RELOAD, 0))
            return
        }
        synchronized(this) {
            onReload = true
            val time = System.currentTimeMillis()
            CompletableFuture.runAsync {
                PluginReloadStartEvent().call()
                runCatching {
                    managers.forEach {
                        it.preReload()
                    }
                    val resource = GlobalResource()
                    val index = TaskIndex(managers.size)

                    fun managerReload() {
                        if (index.current < managers.size) {
                            val manager = synchronized(index) {
                                managers[index.current++]
                            }
                            info("Loading ${manager.javaClass.simpleName}...")
                            manager.reload(resource) {
                                managerReload()
                            }
                        } else {
                            PackGenerator.generate {
                                onReload = false
                                managers.forEach {
                                    it.postReload()
                                }
                                val result = ReloadResult(ReloadState.SUCCESS, System.currentTimeMillis() - time)
                                PluginReloadedEvent(result).call()
                                consumer.accept(result)
                            }
                        }
                    }
                    managerReload()
                }.onFailure { e ->
                    warn(
                        "Unable to reload.",
                        "Reason: ${e.message}"
                    )
                    onReload = false
                    consumer.accept(ReloadResult(ReloadState.FAIL, System.currentTimeMillis() - time))
                }
            }.handle { _, e ->
                e.printStackTrace()
            }
        }
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
    override fun getWidth(target: Char): Int = TextManager.getWidth(target)
    override fun getBedrockAdapter(): BedrockAdapter = bedrockAdapter
    override fun getAudiences(): BukkitAudiences = audience
    override fun getHudPlayer(player: Player): HudPlayer = PlayerManager.getHudPlayer(player)


    override fun loadAssets(prefix: String, dir: File) {
        loadAssets(prefix, {
            File(dir, it).run {
                if (!exists()) mkdir()
            }
        }) { s, i ->
            File(dir, s).outputStream().buffered().use {
                i.copyTo(it)
            }
        }
    }

    override fun loadAssets(prefix: String, consumer: BiConsumer<String, InputStream>) {
        loadAssets(prefix, {}) { s, i ->
            consumer.accept(s, i)
        }
    }

    private fun loadAssets(prefix: String, dir: (String) -> Unit, consumer: (String, InputStream) -> Unit) {
        JarFile(file).use {
            it.entries().asSequence().sortedBy { dir ->
                dir.name.length
            }.forEach { entry ->
                if (!entry.name.startsWith(prefix)) return@forEach
                if (entry.name.length <= prefix.length + 1) return@forEach
                val name = entry.name.substring(prefix.length + 1)
                if (entry.isDirectory) {
                    dir(name)
                } else getResource(entry.name)?.buffered()?.use { stream ->
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
    override fun getCompassManager(): CompassManager = CompassManagerImpl
    override fun getConfigManager(): ConfigManager = ConfigManagerImpl
    override fun isOnReload(): Boolean = onReload
    override fun getDefaultKey(): Key = DEFAULT_KEY
}