package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.*
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.format.TextColor
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object PlayerHeadManager : BetterHudManager {

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headLock = Collections.synchronizedMap(WeakHashMap<String, HudPlayerHeadImpl>())
    private val headCache = ConcurrentHashMap<String, CachedHead>()
    private val headMap = HashMap<String, HudHead>()

    private val headNameComponent = ConcurrentHashMap<ShaderGroup, String>()


    @Synchronized
    fun getHead(group: ShaderGroup) = headNameComponent[group]
    @Synchronized
    fun setHead(group: ShaderGroup, string: String) {
        headNameComponent[group] = string
    }

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

    fun addSkinProvider(provider: PlayerSkinProvider) {
        skinProviders.add(provider)
    }

    override fun start() {
        skinProviders.add(MineToolsProvider())
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
                    headLock.remove(next.name)
                }
            }
        }
    }

    fun provideSkin(playerName: String): String {
        for (skinProvider in skinProviders) {
            runCatching {
                val value = PlayerManagerImpl.getHudPlayer(playerName)?.let {
                    skinProvider.provide(it)
                } ?: skinProvider.provide(playerName)
                if (value != null) return value
            }
        }
        return defaultProviders.provide(playerName)
    }

    fun provideHead(playerName: String): HudPlayerHead {
        return headCache[playerName]?.update() ?: run {
            headLock.computeIfAbsent(playerName) {
                CompletableFuture.runAsync {
                    val head = CachedHead(playerName, HudPlayerHeadImpl.of(playerName))
                    headCache[playerName] = head
                    headLock.remove(playerName)
                }
                loadingHead()
            }
        }
    }

    fun getHead(name: String) = synchronized(headMap) {
        headMap[name]
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        synchronized(this) {
            headMap.clear()
            headNameComponent.clear()
        }
        headLock.clear()
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
        DATA_FOLDER.subFolder("heads").forEachAllYaml(sender) { file, s, yamlObject ->
            runWithExceptionHandling(sender, "Unable to load this head: $s in ${file.name}") {
                headMap.putSync("head", s) {
                    val head = HudHead(file.path, s, yamlObject)
                    val pixel = head.pixel
                    val targetFile = ArrayList(resource.textures).apply {
                        val encode = "pixel_$pixel".encodeKey()
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
            }
        }
    }

    override fun postReload() {
        headNameComponent.clear()
    }

    override fun end() {
    }
}