package kr.toxicity.hud.manager

import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.awt.Color
import java.awt.image.BufferedImage

object PlayerHeadManager: BetterHudManager {

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headMap = HashMap<String, HudHead>()

    override fun start() {
        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
            skinProviders.add(SkinsRestorerSkinProvider())
        }
        if (!Bukkit.getServer().onlineMode) {
            skinProviders.add(HttpSkinProvider())
        }
    }

    fun provideSkin(player: Player): String {
        for (skinProvider in skinProviders) {
            val value = skinProvider.provide(player)
            if (value != null) return value
        }
        return defaultProviders.provide(player)
    }

    fun getHead(name: String) = synchronized(headMap) {
        headMap[name]
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        val saveLocation = ArrayList(resource.textures).apply {
            add("head")
        }
        synchronized(headMap) {
            headMap.clear()
        }
        DATA_FOLDER.subFolder("heads").forEachAllYamlAsync({ file, s, configurationSection ->
            runCatching {
                headMap.putSync("head", s) {
                    val head = HudHead(file.path, s, configurationSection)
                    val pixel = head.pixel
                    val targetFile = ArrayList(saveLocation).apply {
                        add("pixel_$pixel.png".encodeFile())
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