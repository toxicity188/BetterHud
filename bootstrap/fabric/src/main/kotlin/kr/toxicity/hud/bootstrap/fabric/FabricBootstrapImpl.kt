package kr.toxicity.hud.bootstrap.fabric

import kr.toxicity.hud.BetterHudImpl
import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.BetterHudLogger
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.api.fabric.FabricBootstrap
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.scheduler.HudScheduler
import kr.toxicity.hud.api.volatilecode.VolatileCodeHandler
import kr.toxicity.hud.bootstrap.fabric.manager.ModuleManager
import kr.toxicity.hud.bootstrap.fabric.player.HudPlayerFabric
import kr.toxicity.hud.manager.DatabaseManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.manager.ShaderManagerImpl
import kr.toxicity.hud.pack.PackUploader
import kr.toxicity.hud.util.*
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ServerLevelData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URLClassLoader
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class FabricBootstrapImpl: FabricBootstrap, DedicatedServerModInitializer {

    companion object {
        @JvmStatic
        val MOD_ID = "betterhud"
        @JvmStatic
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }

    private val scheduler = FabricScheduler()


    private var logger = object : BetterHudLogger {
        override fun info(vararg message: String) {
            synchronized(LOGGER) {
                message.forEach {
                    LOGGER.info(it)
                }
            }
        }
        override fun warn(vararg message: String) {
            synchronized(LOGGER) {
                message.forEach {
                    LOGGER.warn(it)
                }
            }
        }
    }

    private lateinit var server: MinecraftServer
    private lateinit var dataFolder: File
    lateinit var audiences: FabricServerAudiences
        private set
    private lateinit var volatileCode: FabricVolatileCode
    private lateinit var version: String
    private lateinit var core: BetterHudImpl

    private var latestVersion: String? = null

    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register {
            server = it
            dataFolder = FabricLoader.getInstance().gameDir.resolve("mods").resolve("BetterHud").toFile()
            version = FabricLoader.getInstance().getModContainer(MOD_ID).map { c ->
                c.metadata.version.friendlyString
            }.orElse("unknown")
            core = BetterHudImpl(this).apply {
                BetterHudAPI.inst(this)
            }
            audiences = FabricServerAudiences.builder(it).build()
            volatileCode = FabricVolatileCode()

            core.start()
            ShaderManagerImpl.addTagBuilder("CreateOtherShader") {
                emptyList()
            }
            ModuleManager.start()
            scheduler.asyncTask {
                logger.info("Mod enabled.")
                core.reload()
                runWithExceptionHandling(CONSOLE, "Unable to get latest version.") {
                    HttpClient.newHttpClient().sendAsync(
                        HttpRequest.newBuilder()
                            .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=115559/"))
                            .GET()
                            .build(), HttpResponse.BodyHandlers.ofString()
                    ).thenAccept { callback ->
                        val v = callback.body()
                        latestVersion = v
                        if (version() != v) {
                            warn("New version found: $v")
                            warn("Download: https://modrinth.com/plugin/betterhud2")
                        }
                    }
                }
            }
        }
        ServerLifecycleEvents.SERVER_STOPPED.register {
            scheduler.stopAll()
            audiences.close()
            logger.info("Mod disabled.")
        }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            FabricCommand(this).register(dispatcher)
        }
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, _, _ ->
            val player = handler.player
            latestVersion?.let { latest ->
                if (version() != latest) {
                    val audience = audiences.audience(player)
                    audience.info("New BetterHud version found: $latest")
                    audience.info(
                        Component.text("Download: https://modrinth.com/plugin/betterhud2")
                            .clickEvent(
                                ClickEvent.clickEvent(
                                    ClickEvent.Action.OPEN_URL,
                                    "https://modrinth.com/plugin/betterhud2"
                                )))
                }
            }
            register(player)
        })
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionEvents.Disconnect { handler, _ ->
            disconnect(handler.player)
        })
    }

    private fun register(player: ServerPlayer) {
        PlayerManagerImpl.addHudPlayer(player.uuid) {
            CompletableFuture.supplyAsync {
                val impl = HudPlayerFabric(player, audiences.audience(player))
                DatabaseManagerImpl.currentDatabase.load(impl)
                task {
                    taskLater(20) {
                        sendResourcePack(impl)
                    }
                }
                impl
            }.join()
        }
    }
    private fun disconnect(player: ServerPlayer) {
        PlayerManagerImpl.removeHudPlayer(player.uuid)?.let {
            it.cancel()
            asyncTask {
                it.save()
            }
        }
    }

    override fun logger(): BetterHudLogger = logger

    override fun dataFolder(): File = dataFolder

    override fun console(): Audience {
        return audiences.console()
    }

    override fun core(): BetterHud = core

    override fun jarFile(): File = File(javaClass.getProtectionDomain().codeSource.location.toURI())

    override fun version(): String = version

    override fun scheduler(): HudScheduler = scheduler

    override fun volatileCode(): VolatileCodeHandler = volatileCode
    override fun resource(path: String): InputStream? = javaClass.getResourceAsStream("/$path")?.buffered()

    override fun useLegacyFont(): Boolean = false

    override fun startMetrics() {
    }

    override fun endMetrics() {
    }

    override fun sendResourcePack(player: HudPlayer) {
        PackUploader.server?.let {
            val info = ResourcePackRequest.resourcePackRequest()
                .packs(listOf(
                    ResourcePackInfo.resourcePackInfo(it.uuid, URI.create(it.url), it.digestString)
                ))
                .replace(true)
                .prompt(Component.empty())
                .build()
            player.audience().sendResourcePacks(info)
        }
    }

    override fun sendResourcePack() {
        PackUploader.server?.let {
            val info = ResourcePackRequest.resourcePackRequest()
                .packs(listOf(
                    ResourcePackInfo.resourcePackInfo(it.uuid, URI.create(it.url), it.digestString)
                ))
                .replace(true)
                .prompt(Component.empty())
                .build()
            PlayerManagerImpl.allHudPlayer.forEach { player ->
                player.audience().sendResourcePacks(info)
            }
        }
    }


    override fun minecraftVersion(): String = "1.21.1"

    override fun mcmetaVersion(): Int = 34

    private val uuidMap = ConcurrentHashMap<String, UUID>()

    private fun createUUID(string: String): UUID {
        return uuidMap.computeIfAbsent(string) {
            var uuid = UUID.randomUUID()
            while (uuidMap.values.contains(uuid)) {
                uuid = UUID.randomUUID()
            }
            uuid
        }
    }

    fun wrap(world: ServerLevel): WorldWrapper {
        val levelName = (world.levelData as ServerLevelData).levelName
        return WorldWrapper(
            levelName,
            createUUID(levelName)
        )
    }

    override fun world(name: String): WorldWrapper? {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace(name)))?.let {
            wrap(it)
        }
    }

    override fun worlds(): List<WorldWrapper> = server.allLevels.map {
        wrap(it)
    }

    override fun loader(): URLClassLoader {
        val loader = javaClass.classLoader
        return (javaClass.classLoader.javaClass.declaredFields.first {
            URLClassLoader::class.java.isAssignableFrom(it.type)
        }).apply {
            isAccessible = true
        }[loader] as URLClassLoader
    }
}