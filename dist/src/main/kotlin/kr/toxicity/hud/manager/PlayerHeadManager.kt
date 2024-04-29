package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.jodah.expiringmap.ExpiringMap
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object PlayerHeadManager : BetterHudManager {

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headLock = Collections.newSetFromMap(WeakHashMap<String, Boolean>())
    private val headCache = ExpiringMap.builder()
        .expiration(5, TimeUnit.MINUTES)
        .expirationListener { k1: String, _: HudPlayerHead ->
            headLock.remove(k1)
        }
        .build<String, HudPlayerHead>()
    private val headMap = HashMap<String, HudHead>()

    private var steve = HudPlayerHeadImpl.allBlack

    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            skinProviders.add(SkinsRestorerSkinProvider())
        }
        skinProviders.add(MineToolsProvider())
        if (!Bukkit.getServer().onlineMode) {
            skinProviders.add(HttpSkinProvider())
        }
        PLUGIN.getResource("steve.png")?.buffered()?.use {
            val image = it.toImage()
            steve = HudPlayerHeadImpl((0..63).map { i ->
                TextColor.color(image.getRGB(i % 8, i / 8))
            })
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
            val get = headCache[playerName]
            if (get == null && !headLock.contains(playerName)) {
                headLock.add(playerName)
                CompletableFuture.runAsync {
                    headCache[playerName] = HudPlayerHeadImpl.of(playerName)
                    headLock.remove(playerName)
                }
            }
            get ?: steve
        }
    }

    fun getHead(name: String) = synchronized(headMap) {
        headMap[name]
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        synchronized(headMap) {
            headMap.clear()
        }
        headCache.clear()
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