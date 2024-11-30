package kr.toxicity.hud

import kr.toxicity.hud.api.BetterHud
import kr.toxicity.hud.api.BetterHudBootstrap
import kr.toxicity.hud.api.BetterHudDependency
import kr.toxicity.hud.api.manager.*
import kr.toxicity.hud.api.plugin.ReloadState
import kr.toxicity.hud.api.plugin.ReloadState.Failure
import kr.toxicity.hud.api.plugin.ReloadState.Success
import kr.toxicity.hud.dependency.DependencyInjector
import kr.toxicity.hud.manager.*
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import java.io.File
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.jar.JarFile

@Suppress("UNUSED")
class BetterHudImpl(val bootstrap: BetterHudBootstrap) : BetterHud {

    init {
        if (!bootstrap.dataFolder().exists() && !bootstrap.isVelocity) loadAssets("default", bootstrap.dataFolder().apply {
            mkdir()
        })
        val injector = DependencyInjector(bootstrap.version(), bootstrap.dataFolder(), bootstrap.logger(), bootstrap.loader())
        BetterHudDependency.dependencies().forEach {
            if (it.platforms.any { p ->
                p.match(bootstrap)
            }) injector.load(it)
        }
    }

    private val managers: List<BetterHudManager> by lazy {
        listOf(
            ConfigManagerImpl,
            MinecraftManager,
            CommandManager,
            DatabaseManagerImpl,

            ListenerManagerImpl,
            PlaceholderManagerImpl,
            TriggerManagerImpl,

            BackgroundManager,
            ImageManager,
            TextManagerImpl,
            PlayerHeadManager,
            LayoutManager,
            HudManagerImpl,
            PopupManagerImpl,
            CompassManagerImpl,

            ShaderManagerImpl,
            PlayerManagerImpl,
        )
    }

    fun start() {
        managers.forEach {
            it.start()
        }
    }

    private val reloadStartTask = ArrayList<() -> Unit>()
    private val reloadEndTask = ArrayList<(ReloadState) -> Unit>()

    override fun addReloadStartTask(runnable: Runnable) {
        reloadStartTask += {
            runnable.run()
        }
    }

    override fun addReloadEndTask(runnable: Consumer<ReloadState>) {
        reloadEndTask += {
            runnable.accept(it)
        }
    }

    @Volatile
    private var onReload = false

    override fun reload(sender: Audience): ReloadState {
        synchronized(this) {
            if (onReload) {
                return ReloadState.ON_RELOAD
            }
            onReload = true
        }
        val time = System.currentTimeMillis()
        val result = CompletableFuture.supplyAsync {
            reloadStartTask.forEach {
                it()
            }
            val result = runWithExceptionHandling(sender, "Unable to reload.") {
                managers.forEach {
                    it.preReload()
                }
                val resource = GlobalResource()
                managers.forEach {
                    it.reload(sender, resource)
                }
                managers.forEach {
                    it.postReload()
                }
                Success(System.currentTimeMillis() - time, PackGenerator.generate(sender))
            }.getOrElse {
                Failure(it)
            }
            reloadEndTask.forEach {
                it(result)
            }
            result
        }.join()
        synchronized(this) {
            onReload = false
        }
        return result
    }

    override fun bootstrap(): BetterHudBootstrap = bootstrap


    fun end() {
        managers.forEach {
            it.end()
        }
        BOOTSTRAP.endMetrics()
        DatabaseManagerImpl.currentDatabase.close()
    }

    override fun getWidth(codepoint: Int): Int = TextManagerImpl.getWidth(codepoint) ?: 3

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
        JarFile(bootstrap.jarFile()).use {
            it.entries().asSequence().forEach { entry ->
                if (!entry.name.startsWith(prefix)) return@forEach
                if (entry.name.length <= prefix.length + 1) return@forEach
                val name = entry.name.substring(prefix.length + 1)
                if (!entry.isDirectory) it.getInputStream(entry).buffered().use { stream ->
                    consumer(name, stream)
                }
            }
        }
    }

    override fun getEncodedNamespace(): String = NAME_SPACE_ENCODED
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
    override fun getPlayerManager(): PlayerManager = PlayerManagerImpl
    override fun getTextManager(): TextManager = TextManagerImpl
    override fun isOnReload(): Boolean = onReload
    override fun getDefaultKey(): Key = DEFAULT_KEY
    override fun translate(locale: String, key: String): String? = TextManagerImpl.translate(locale, key)
}