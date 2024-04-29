package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

object PlayerHeadManager : BetterHudManager {

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headCache = ExpiringMap.builder()
        .expiration(5, TimeUnit.MINUTES)
        .build<String, HudPlayerHead>()
    private val headMap = HashMap<String, HudHead>()

    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            skinProviders.add(SkinsRestorerSkinProvider())
        }
        skinProviders.add(MineToolsProvider())
        if (!Bukkit.getServer().onlineMode) {
            skinProviders.add(HttpSkinProvider())
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
        return synchronized(headCache) {
            headCache.computeIfAbsent(playerName) {
                HudPlayerHeadImpl(playerName)
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
        synchronized(headCache) {
            headCache.clear()
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