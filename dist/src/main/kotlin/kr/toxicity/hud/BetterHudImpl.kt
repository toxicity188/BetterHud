package kr.toxicity.hud

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.bedrock.BedrockAdapter
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
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.scheduler.FoliaScheduler
import kr.toxicity.hud.scheduler.StandardScheduler
import kr.toxicity.hud.skript.SkriptManager
import kr.toxicity.hud.util.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.jar.JarFile

class BetterHudImpl: BetterHud() {
    private val managers = listOf(
        ConfigManager,
        CommandManager,
        CompatibilityManager,
        SkriptManager,
        ModuleManager,
        DatabaseManagerImpl,

        ListenerManagerImpl,
        PlaceholderManagerImpl,
        TriggerManagerImpl,

        ImageManager,
        TextManager,
        PlayerHeadManager,
        LayoutManager,
        HudManagerImpl,
        PopupManagerImpl,

        ShaderManager,
        PlayerManager
    )

    private lateinit var nms: NMS
    private lateinit var audience: BukkitAudiences
    private lateinit var bedrockAdapter: BedrockAdapter

    private val isFolia = runCatching {
        Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler")
        true
    }.getOrDefault(false)

    private val scheduler = if (isFolia) FoliaScheduler() else StandardScheduler()

    override fun onEnable() {
        runCatching {
            Metrics(this, 21287)
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
        task {
            reload()
            Bukkit.getOnlinePlayers().forEach {
                PlayerManager.register(it)
            }
            info("Plugin enabled.")
        }
    }

    private var onReload = false

    override fun reload(): ReloadResult {
        if (onReload) return ReloadResult(ReloadState.STILL_ON_RELOAD, 0)
        onReload = true
        val time = System.currentTimeMillis()
        return runCatching {
            managers.forEach {
                it.preReload()
            }
            val resource = GlobalResource()
            managers.forEach {
                it.reload(resource)
            }
            managers.forEach {
                it.postReload()
            }
            onReload = false
            val result = ReloadResult(ReloadState.SUCCESS, System.currentTimeMillis() - time)
            task {
                PluginReloadedEvent(result).call()
            }
            result
        }.getOrElse { e ->
            warn("Unable to reload.")
            warn("Reason: ${e.message}")
            onReload = false
            ReloadResult(ReloadState.FAIL, System.currentTimeMillis() - time)
        }
    }


    override fun onDisable() {
        audience.close()
        managers.forEach {
            it.end()
        }
        info("Plugin disabled.")
    }

    override fun getNMS(): NMS = nms
    override fun getWidth(target: Char): Int = TextManager.getWidth(target)
    override fun getBedrockAdapter(): BedrockAdapter = bedrockAdapter
    override fun getAudiences(): BukkitAudiences = audience
    override fun getHudPlayer(player: Player): HudPlayer = PlayerManager.getHudPlayer(player)

    override fun loadAssets(prefix: String, dir: File) {
        JarFile(file).use {
            it.entries().asSequence().sortedBy { dir ->
                dir.name.length
            }.forEach { entry ->
                if (!entry.name.startsWith(prefix)) return@forEach
                if (entry.name.length <= prefix.length + 1) return@forEach
                val name = entry.name.substring(prefix.length + 1)
                val file = File(dir, name)
                if (entry.isDirectory) {
                    if (!file.exists()) file.mkdir()
                } else {
                    getResource(entry.name)?.buffered()?.use { stream ->
                        if (!file.exists()) {
                            file.createNewFile()
                            file.outputStream().buffered().use { fos ->
                                stream.copyTo(fos)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getScheduler(): HudScheduler = scheduler
    override fun isFolia(): Boolean = isFolia
    override fun getPlaceholderManager(): PlaceholderManager = PlaceholderManagerImpl
    override fun getListenerManager(): ListenerManager = ListenerManagerImpl
    override fun getPopupManager(): PopupManager = PopupManagerImpl
    override fun getTriggerManager(): TriggerManager = TriggerManagerImpl
    override fun getHudManager(): HudManager = HudManagerImpl
    override fun getDatabaseManager(): DatabaseManager = DatabaseManagerImpl
    override fun isOnReload(): Boolean = onReload
}