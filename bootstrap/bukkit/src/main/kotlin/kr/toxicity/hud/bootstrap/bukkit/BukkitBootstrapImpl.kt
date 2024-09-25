package kr.toxicity.hud.bootstrap.bukkit

import kr.toxicity.hud.BetterHudImpl
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.BetterHudLogger
import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.api.bukkit.BukkitBootstrap
import kr.toxicity.hud.api.bukkit.bedrock.BedrockAdapter
import kr.toxicity.hud.api.bukkit.event.*
import kr.toxicity.hud.api.bukkit.nms.NMS
import kr.toxicity.hud.api.bukkit.nms.NMSVersion
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bedrock.FloodgateAdapter
import kr.toxicity.hud.bedrock.GeyserAdapter
import kr.toxicity.hud.bootstrap.bukkit.manager.CompatibilityManager
import kr.toxicity.hud.bootstrap.bukkit.manager.ModuleManager
import kr.toxicity.hud.bootstrap.bukkit.player.HudPlayerBukkit
import kr.toxicity.hud.bootstrap.bukkit.util.MinecraftVersion
import kr.toxicity.hud.bootstrap.bukkit.util.call
import kr.toxicity.hud.pack.PackUploader
import kr.toxicity.hud.player.head.HttpSkinProvider
import kr.toxicity.hud.bootstrap.bukkit.player.head.SkinsRestorerSkinProvider
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.placeholder.PlaceholderTask
import kr.toxicity.hud.bootstrap.bukkit.player.location.GPSLocationProvider
import kr.toxicity.hud.scheduler.FoliaScheduler
import kr.toxicity.hud.scheduler.StandardScheduler
import kr.toxicity.hud.util.*
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.function.Function
import java.util.concurrent.CompletableFuture

@Suppress("UNUSED")
class BukkitBootstrapImpl: BukkitBootstrap, JavaPlugin() {

    private val listener = object : Listener {}

    private val isFolia = runCatching {
        Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler")
        true
    }.getOrDefault(false)
    private val isPaper = isFolia || runCatching {
        Class.forName("com.destroystokyo.paper.profile.PlayerProfile")
        true
    }.getOrDefault(false)

    private val scheduler = if (isFolia) FoliaScheduler(this) else StandardScheduler(this)
    private val updateTask = ArrayList<PlaceholderTask>()

    private val log = object : BetterHudLogger {
        override fun info(vararg message: String) {
            val l = logger
            synchronized(l) {
                message.forEach {
                    l.info(it)
                }
            }
        }
        override fun warn(vararg message: String) {
            val l = logger
            synchronized(l) {
                message.forEach {
                    l.warning(it)
                }
            }
        }
    }

    private val core = BetterHudImpl(this).apply {
        BetterHudAPI.inst(this)
        addReloadStartTask {
            PluginReloadStartEvent().call()
        }
        addReloadEndTask {
            PluginReloadedEvent(it).call()
        }
        addReloadStartTask {
            HandlerList.unregisterAll(listener)
        }
        addReloadEndTask {
            updateTask.clear()
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                DATA_FOLDER.subFolder("placeholders").forEachAllYaml(CONSOLE) { file, s, yamlObject ->
                    runWithExceptionHandling(CONSOLE, "Unable to read this placeholder task: $s in ${file.name}") {
                        val variable = yamlObject.get("variable")?.asString().ifNull("variable not set.")
                        val placeholder = yamlObject.get("placeholder")?.asString().ifNull("placeholder not set.")
                        val update = yamlObject.getAsInt("update", 1).coerceAtLeast(1)
                        val async = yamlObject.getAsBoolean("async", false)
                        updateTask.add(object : PlaceholderTask {
                            override val tick: Int
                                get() = update
                            override val async: Boolean
                                get() = async

                            override fun invoke(p1: HudPlayer) {
                                runCatching {
                                    p1.variableMap[variable] = PlaceholderAPI.setPlaceholders(p1.bukkitPlayer, placeholder)
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun update(hudPlayer: HudPlayer) {
        val task = updateTask.filter {
            hudPlayer.tick % it.tick == 0L
        }
        if (task.isNotEmpty()) {
            val syncTask = ArrayList<PlaceholderTask>()
            task.forEach {
                if (it.async) it(hudPlayer) else syncTask.add(it)
            }
            if (syncTask.isEmpty()) return
            task(hudPlayer.location()) {
                syncTask.forEach {
                    it(hudPlayer)
                }
            }
        }
    }

    private lateinit var bedrockAdapter: BedrockAdapter
    private lateinit var nms: NMS
    private lateinit var audiences: BukkitAudiences

    override fun isFolia(): Boolean = isFolia
    override fun volatileCode(): NMS = nms
    override fun bedrockAdapter(): BedrockAdapter = bedrockAdapter

    override fun isPaper(): Boolean = isPaper
    override fun scheduler(): HudScheduler = scheduler
    override fun jarFile(): File = file
    override fun core(): BetterHud = core
    override fun version(): String = description.version

    override fun onEnable() {
        val pluginManager = Bukkit.getPluginManager()
        nms = when (MinecraftVersion.current) {
            MinecraftVersion.version1_21, MinecraftVersion.version1_21_1 -> kr.toxicity.hud.nms.v1_21_R1.NMSImpl()
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
        audiences = BukkitAudiences.create(this)
        if (pluginManager.isPluginEnabled("GPS")) PlayerManagerImpl.addLocationProvider(GPSLocationProvider())
        pluginManager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun join(e: PlayerJoinEvent) {
                register(e.player)
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                PlayerManagerImpl.removeHudPlayer(player.uniqueId)?.let {
                    it.cancel()
                    HudPlayerQuitEvent(it).call()
                    asyncTask {
                        it.save()
                    }
                }
            }
        }, this)
        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            PlayerHeadManager.addSkinProvider(SkinsRestorerSkinProvider())
        }
        if (!Bukkit.getServer().onlineMode) {
            PlayerHeadManager.addSkinProvider(HttpSkinProvider())
        }
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderManagerImpl.stringContainer.addPlaceholder("papi", object : HudPlaceholder<String> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: List<String>, reason: UpdateEvent): Function<HudPlayer, String> {
                    val format = "%${args[0]}%"
                    return Function { player ->
                        runCatching {
                            PlaceholderAPI.setPlaceholders(player.bukkitPlayer, format)
                        }.getOrDefault("<error>")
                    }
                }
            })
        }
        ShaderManagerImpl.addTagBuilder("CreateOtherShader") {
            CreateShaderEvent().apply {
                call()
            }.lines
        }
        ModuleManager.start()
        CompatibilityManager.start()
        Bukkit.getOnlinePlayers().forEach {
            register(it)
        }
        runWithExceptionHandling(CONSOLE, "Unable to get latest version.") {
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
                                val audience = audiences.player(player)
                                audience.info("New BetterHud version found: $body")
                                audience.info(
                                    Component.text("Download: https://www.spigotmc.org/resources/115559")
                                    .clickEvent(
                                        ClickEvent.clickEvent(
                                        ClickEvent.Action.OPEN_URL,
                                        "https://www.spigotmc.org/resources/115559"
                                    )))
                            }
                        }
                    }, this)
                }
            }
        }
        getCommand("betterhud")?.setExecutor(object : TabExecutor {
            override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
                CommandManager.command.execute(p0.toWrapper() ?: return false, p3.toList())
                return true
            }

            private fun CommandSender.toWrapper() = when (this) {
                is Player -> PlayerManagerImpl.getHudPlayer(uniqueId)
                is ConsoleCommandSender -> object : CommandSourceWrapper {
                    override fun type(): CommandSourceWrapper.Type = CommandSourceWrapper.Type.CONSOLE
                    override fun audience(): Audience = this@BukkitBootstrapImpl.console()
                    override fun isOp(): Boolean = true
                    override fun hasPermission(perm: String): Boolean = true
                }
                else -> null
            }

            override fun onTabComplete(
                p0: CommandSender,
                p1: Command,
                p2: String,
                p3: Array<String>
            ): List<String>? {
                return CommandManager.command.tabComplete(p0.toWrapper() ?: return null, p3.toList())
            }
        })
        core.start()
        scheduler.asyncTask {
            core.reload()
            log.info(
                "Minecraft version: ${MinecraftVersion.current}, NMS version: ${nms.version}",
                "Plugin enabled."
            )
        }
    }

    override fun onDisable() {
        core.end()
        audiences.close()
        metrics?.shutdown()
        log.info("Plugin disabled.")
    }

    fun register(player: Player) {
        if (ConfigManagerImpl.disableToBedrockPlayer && bedrockAdapter.isBedrockPlayer(player.uniqueId)) return
        val adaptedPlayer = if (isFolia) nms.getFoliaAdaptedPlayer(player) else player
        PlayerManagerImpl.addHudPlayer(adaptedPlayer.uniqueId) {
            CompletableFuture.supplyAsync {
                val impl = HudPlayerBukkit(adaptedPlayer, if (player is Audience) player else audiences.player(player))
                DatabaseManagerImpl.currentDatabase.load(impl)
                task {
                    taskLater(20) {
                        sendResourcePack(impl)
                    }
                    HudPlayerJoinEvent(impl).call()
                }
                impl
            }.join()
        }
    }

    override fun resource(path: String): InputStream? = getResource(path)
    override fun logger(): BetterHudLogger = log
    override fun dataFolder(): File = dataFolder
    override fun console(): Audience = Bukkit.getConsoleSender().let {
        if (it is Audience) it else audiences.sender(it)
    }

    private var metrics: Metrics? = null
    override fun startMetrics() {
        if (metrics == null) metrics = Metrics(this, BetterHud.BSTATS_ID_BUKKIT)
    }

    override fun endMetrics() {
        metrics?.shutdown()
        metrics = null
    }

    override fun sendResourcePack(hudPlayer: HudPlayer) {
        PackUploader.server?.let {
            if (nms.version >= NMSVersion.V1_20_R3) {
                (hudPlayer.handle() as Player).setResourcePack(it.uuid, it.url, it.digest, null, false)
            } else {
                (hudPlayer.handle() as Player).setResourcePack(it.url, it.digest, null, false)
            }
        }
    }
    override fun sendResourcePack() {
        PackUploader.server?.let {
            if (nms.version >= NMSVersion.V1_20_R3) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.setResourcePack(it.uuid, it.url, it.digest, null, false)
                }
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.setResourcePack(it.url, it.digest, null, false)
                }
            }
        }
    }

    override fun minecraftVersion(): String = MinecraftVersion.current.toString()
    override fun mcmetaVersion(): Int = nms.version.metaVersion
    override fun triggerListener(): Listener = listener
    override fun useLegacyFont(): Boolean = nms.version.version <= 18
}