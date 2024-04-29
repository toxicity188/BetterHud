package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object PlayerHeadManager : BetterHudManager {

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headLock = WeakHashMap<String, HudPlayerHeadImpl>()
    private val headCache = ConcurrentHashMap<String, CachedHead>()
    private val headMap = HashMap<String, HudHead>()

    private class CachedHead(
        val name: String,
        private val head: HudPlayerHeadImpl
    ) {
        @Volatile
        private var i = 60
        fun update(): HudPlayerHeadImpl {
            i = 60
            return head
        }
        fun check(): Boolean {
            return --i <= 0
        }
    }

    private val loadingHeadMap = HashMap<String, HudPlayerHeadImpl>()
    private var loadingHead = {
        HudPlayerHeadImpl.allBlack
    }

    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            skinProviders.add(SkinsRestorerSkinProvider())
        }
        skinProviders.add(MineToolsProvider())
        if (!Bukkit.getServer().onlineMode) {
            skinProviders.add(HttpSkinProvider())
        }
        PLUGIN.loadAssets("skin") { s, stream ->
            val image = stream.toImage()
            loadingHeadMap[s.substringBeforeLast('.')] = HudPlayerHeadImpl((0..63).map { i ->
                TextColor.color(image.getRGB(i % 8, i / 8))
            })
        }
        asyncTaskTimer(20, 20) {
            val iterator = headCache.values.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.check()) {
                    iterator.remove()
                    synchronized(headLock) {
                        headLock.remove(next.name)
                    }
                }
            }
        }
    }

    fun provideSkin(playerName: String): String {
        for (skinProvider in skinProviders) {
            runCatching {
                val value = skinProvider.provide(playerName)
                if (value != null) return value
            }
        }
        return defaultProviders.provide(playerName)
    }

    fun provideHead(playerName: String): HudPlayerHead {
        return synchronized(headLock) {
            headCache[playerName]?.update() ?: run {
                headLock.computeIfAbsent(playerName) {
                    CompletableFuture.runAsync {
                        headCache[playerName] = CachedHead(playerName, HudPlayerHeadImpl.of(playerName))
                        synchronized(headLock) {
                            headLock.remove(playerName)
                        }
                    }
                    loadingHead()
                }
            }
        }
    }

    fun getHead(name: String) = synchronized(headMap) {
        headMap[name]
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        synchronized(headMap) {
            headMap.clear()
        }
        synchronized(headLock) {
            headLock.clear()
        }
        headCache.clear()
        loadingHead = when (val name = ConfigManagerImpl.loadingHead) {
            "random" -> {
                {
                    if (loadingHeadMap.isNotEmpty()) loadingHeadMap.values.random() else HudPlayerHeadImpl.allBlack
                }
            }
            else -> {
                {
                    loadingHeadMap[name] ?: HudPlayerHeadImpl.allBlack
                }
            }
        }
        DATA_FOLDER.subFolder("heads").forEachAllYamlAsync({ file, s, configurationSection ->
            runCatching {
                headMap.putSync("head", s) {
                    val head = HudHead(file.path, s, configurationSection)
                    val pixel = head.pixel
                    val targetFile = ArrayList(resource.textures).apply {
                        val encode = "pixel_$pixel".encodeKey()
                        add(encode)
                        add("$encode.png")
                    }
                    PackGenerator.addTask(targetFile) {
                        BufferedImage(pixel, pixel, BufferedImage.TYPE_INT_ARGB).apply {
                            createGraphics().run {
                                color = Color.WHITE
                                fillRect(0, 0, pixel, pixel)
                                dispose()
                            }
                        }.toByteArray()
                    }
                    head
                }
            }.onFailure { e ->
                warn(
                    "Unable to load this head: $s in ${file.name}",
                    "Reason: ${e.message}"
                )
            }
        }, callback)
    }

    override fun end() {
    }
}