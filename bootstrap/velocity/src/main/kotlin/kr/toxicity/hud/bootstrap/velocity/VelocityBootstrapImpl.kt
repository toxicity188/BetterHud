package kr.toxicity.hud.bootstrap.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginDescription
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import kr.toxicity.hud.BetterHudImpl
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.BetterHudLogger
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.scheduler.HudTask
import kr.toxicity.hud.api.velocity.VelocityBootstrap
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler
import kr.toxicity.hud.bootstrap.velocity.manager.ModuleManager
import kr.toxicity.hud.bootstrap.velocity.player.HudPlayerVelocity
import kr.toxicity.hud.manager.CommandManager
import kr.toxicity.hud.manager.DatabaseManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.pack.PackUploader
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bstats.velocity.Metrics
import org.bstats.velocity.Metrics.Factory
import org.slf4j.Logger
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URLClassLoader
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Suppress("UNUSED")
@Plugin(
    id = "BetterHud"
)
class VelocityBootstrapImpl @Inject constructor(
    private val proxyServer: ProxyServer,
    private val logger: Logger,
    private val factory: Factory,
    private val description: PluginDescription,
    @DataDirectory private val dataFolder: Path
): VelocityBootstrap {

    private val scheduler = object : HudScheduler {
        override fun task(runnable: Runnable): HudTask {
            return proxyServer.scheduler.buildTask(this@VelocityBootstrapImpl, runnable)
                .schedule()
                .toHud()
        }
        private fun ScheduledTask.toHud() = object : HudTask {
            private var cancelled = false
            override fun isCancelled(): Boolean = cancelled
            override fun cancel() {
                cancelled = true
                this@toHud.cancel()
            }
        }

        override fun task(location: LocationWrapper, runnable: Runnable): HudTask = task(runnable)

        override fun taskLater(delay: Long, runnable: Runnable): HudTask {
            return proxyServer.scheduler.buildTask(this@VelocityBootstrapImpl, runnable)
                .delay(delay * 50, TimeUnit.MILLISECONDS)
                .schedule()
                .toHud()
        }

        override fun asyncTask(runnable: Runnable): HudTask = task(runnable)

        override fun asyncTaskTimer(delay: Long, period: Long, runnable: Runnable): HudTask {
            return proxyServer.scheduler.buildTask(this@VelocityBootstrapImpl, runnable)
                .delay(delay * 50, TimeUnit.MILLISECONDS)
                .repeat(period * 50, TimeUnit.MILLISECONDS)
                .schedule()
                .toHud()
        }

    }

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
                    l.warn(it)
                }
            }
        }
    }
    private val volatileCode = VelocityVolatileCodeHandler()
    private val core = BetterHudImpl(this).apply {
        BetterHudAPI.inst(this)
    }

    override fun scheduler(): HudScheduler = scheduler
    override fun jarFile(): File = File(javaClass.getProtectionDomain().codeSource.location.toURI())
    override fun core(): BetterHud = core
    override fun console(): Audience = proxyServer.consoleCommandSource
    override fun volatileCode(): VolatileCodeHandler = volatileCode
    override fun version(): String = description.version.orElse("unknown")
    override fun resource(path: String): InputStream? = javaClass.getResourceAsStream("/$path")?.buffered()

    private var latestVersion: String? = null

    @Subscribe
    fun enable(e: ProxyInitializeEvent) {
        if (isDevVersion) logger.warn("This build is dev version - be careful to use it!")
        else runWithExceptionHandling(CONSOLE, "Unable to get latest version.") {
            HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=115559/"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString()
            ).thenAccept {
                val v = it.body()
                latestVersion = v
                if (version() != v) {
                    warn("New version found: $v")
                    warn("Download: https://www.spigotmc.org/resources/115559")
                }
            }
        }
        proxyServer.allPlayers.forEach {
            register(it)
        }
        ModuleManager.start()
        registerCommand()
        core.start()
        scheduler.task {
            core.reload()
            log.info("Plugin enabled.")
        }
    }


    @Subscribe
    fun disable(e: ProxyShutdownEvent) {
        core.end()
        metrics?.shutdown()
        log.info("Plugin disabled.")
    }

    @Subscribe
    fun login(e: PostLoginEvent) {
        latestVersion?.let { latest ->
            if (version() != latest) {
                e.player.info("New BetterHud version found: $latest")
                e.player.info(
                    Component.text("Download: https://www.spigotmc.org/resources/115559")
                        .clickEvent(
                            ClickEvent.clickEvent(
                                ClickEvent.Action.OPEN_URL,
                                "https://www.spigotmc.org/resources/115559"
                            )))
            }
        }
        register(e.player)
    }

    private fun register(player: Player) {
        PlayerManagerImpl.addHudPlayer(player.uniqueId) {
            val impl = HudPlayerVelocity(player)
            asyncTask {
                DatabaseManagerImpl.currentDatabase.load(impl)
                task {
                    sendResourcePack(impl)
                }
            }
            impl
        }
    }

    override fun logger(): BetterHudLogger = log
    override fun dataFolder(): File = File(dataFolder.toFile().parentFile, "BetterHud")


    private var metrics: Metrics? = null
    override fun startMetrics() {
        if (metrics == null) metrics = factory.make(this, BetterHud.BSTATS_ID_VELOCITY)
    }

    override fun endMetrics() {
        metrics?.shutdown()
        metrics = null
    }

    override fun sendResourcePack(player: HudPlayer) {
        PackUploader.server?.let {
            (player.handle() as Player).sendResourcePackOffer(proxyServer.createResourcePackBuilder(it.url)
                .setHash(it.digest)
                .setId(it.uuid)
                .setShouldForce(true)
                .build())
        }
    }
    override fun sendResourcePack() {
        PackUploader.server?.let {
            val info = proxyServer.createResourcePackBuilder(it.url)
                .setHash(it.digest)
                .setId(it.uuid)
                .setShouldForce(true)
                .build()
            proxyServer.allServers.forEach { p ->
                p.sendResourcePacks(info)
            }
        }
    }

    override fun minecraftVersion(): String = "1.21.4"
    override fun mcmetaVersion(): Int = 46
    override fun useLegacyFont(): Boolean = false

    override fun world(name: String): WorldWrapper? = null
    override fun worlds(): List<WorldWrapper> = emptyList()


    override fun loader(): URLClassLoader {
        return javaClass.classLoader as URLClassLoader
    }

    private fun registerCommand() {
        CommandManager.module.build { s: CommandSource ->
            when (s) {
                is ConsoleCommandSource -> BetterHudAPI.inst().bootstrap().consoleSource()
                is Player -> BetterHudAPI.inst().playerManager.getHudPlayer(s.uniqueId)
                else -> null
            }
        }.forEach {
            BrigadierCommand(it).add()
        }
    }

    private fun BrigadierCommand.add() {
        proxyServer.commandManager.register(
            proxyServer.commandManager.metaBuilder(this).build(),
            this
        )
    }
}