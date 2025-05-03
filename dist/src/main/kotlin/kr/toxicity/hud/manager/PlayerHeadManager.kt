package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.element.HeadElement
import kr.toxicity.hud.layout.HudLayout
import kr.toxicity.hud.pack.PackGenerator
import kr.toxicity.hud.player.head.GameProfileSkinProvider
import kr.toxicity.hud.player.head.HudPlayerHeadImpl
import kr.toxicity.hud.player.head.PlayerSkinProvider
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import net.kyori.adventure.text.format.TextColor
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object PlayerHeadManager : BetterHudManager {

    override val managerName: String = "Player head"
    override val supportExternalPacks: Boolean = true

    private val skinProviders = ArrayList<PlayerSkinProvider>()
    private val defaultProviders = GameProfileSkinProvider()
    private val headLock = Collections.synchronizedMap(WeakHashMap<String, HudPlayerHeadImpl>())
    private val headCache = ExpiringMap.builder()
        .expiration(60, TimeUnit.SECONDS)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build<String, HudPlayerHeadImpl>()
    private val headMap = HashMap<String, HeadElement>()

    private val headNameComponent = ConcurrentHashMap<HudLayout.Identifier, String>()


    @Synchronized
    fun getHead(group: HudLayout.Identifier) = headNameComponent[group]
    @Synchronized
    fun setHead(group: HudLayout.Identifier, string: String) {
        headNameComponent[group] = string
    }

    private val loadingHeadMap = HashMap<String, HudPlayerHeadImpl>()
    private var loadingHead = {
        HudPlayerHeadImpl.allBlack
    }

    fun addSkinProvider(provider: PlayerSkinProvider) {
        skinProviders += provider
    }

    override fun start() {
        PLUGIN.loadAssets("skin") { s, stream ->
            val image = stream.toImage()
            loadingHeadMap[s.substringBeforeLast('.')] = HudPlayerHeadImpl((0..63).map { i ->
                TextColor.color(image.getRGB(i % 8, i / 8))
            }, emptyMap())
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
        return headCache[playerName] ?: run {
            headLock.computeIfAbsent(playerName) {
                CompletableFuture.runAsync {
                    val head = HudPlayerHeadImpl.of(playerName)
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

    override fun preReload() {
        headMap.clear()
        headNameComponent.clear()
        headLock.clear()
        headCache.clear()
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
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
        workingDirectory.subFolder("heads").forEachAllYaml(info.sender) { file, s, yamlObject ->
            runCatching {
                headMap.putSync("head") {
                    val head = HeadElement(s, yamlObject)
                    val pixel = head.pixel
                    PackGenerator.addTask(resource.textures + "${"pixel_$pixel".encodeKey(EncodeManager.EncodeNamespace.TEXTURES)}.png") {
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
            }.handleFailure(info) {
                "Unable to load this head: $s in ${file.name}"
            }
        }
    }

    override fun postReload() {
        headNameComponent.clear()
    }

    override fun end() {
    }
}