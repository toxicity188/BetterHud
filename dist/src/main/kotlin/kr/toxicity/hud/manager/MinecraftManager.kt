package kr.toxicity.hud.manager

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.api.version.MinecraftVersion
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.text.ImageTextScale
import kr.toxicity.hud.util.*
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object MinecraftManager : BetterHudManager {

    private const val ASSETS_LOCATION = "assets/minecraft/textures/"

    override val managerName: String = "Minecraft"
    override val supportExternalPacks: Boolean = false

    private val assetsMap = Collections.synchronizedSet(HashSet<MinecraftAsset>())

    fun applyAll(layout: TextLayout, intGetter: () -> Int): IntKeyMap<ImageTextScale> {
        val map = intKeyMapOf<ImageTextScale>()
        assetsMap.forEach {
            map[intGetter()] = it.toCharWidth(layout)
        }
        return map
    }

    private data class MinecraftAsset(val namespace: String, val width: Int, val height: Int) {
        fun toCharWidth(layout: TextLayout): ImageTextScale {
            return ImageTextScale(
                namespace.replace('/', '_'),
                "minecraft:$namespace.png",
                layout.emoji.location,
                layout.source.textScale?.let { it - height } ?: 0,
                width.toDouble(),
                height.toDouble()
            )
        }
    }

    override fun start() {
    }

    private var previous: MinecraftVersion? = null

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
        if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
            val current = ConfigManagerImpl.minecraftJarVersion ?: BOOTSTRAP.minecraftVersion()
            if (assetsMap.isEmpty() || previous != current) {
                previous = current
            } else return
            assetsMap.clear()
            val cache = DATA_FOLDER.subFolder(".cache")
            httpClient {
                info("Getting minecraft default version...")
                val json = InputStreamReader(send(HttpRequest.newBuilder()
                    .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                    parseJson(it)
                }.asJsonObject
                info("Current minecraft version: $current")
                val file = File(cache, "$current.jar")
                if (!file.exists() || file.length() == 0L) {
                    info("$current.jar doesn't exist. so download it...")
                    ZipOutputStream(file.outputStream().buffered()).use { outputStream ->
                        ZipInputStream(send(HttpRequest.newBuilder()
                            .uri(URI.create(InputStreamReader(send(HttpRequest.newBuilder()
                                .uri(URI.create(json.getAsJsonArray("versions").map {
                                    it.asJsonObject
                                }.first {
                                    it.getAsJsonPrimitive("id").asString.toMinecraftVersion() == current
                                }.getAsJsonPrimitive("url").asString))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                                parseJson(it)
                            }.asJsonObject
                                .getAsJsonObject("downloads")
                                .getAsJsonObject("client")
                                .getAsJsonPrimitive("url")
                                .asString))
                            .GET().build(),
                            HttpResponse.BodyHandlers.ofInputStream()
                        ).body().buffered()).use { inputStream ->
                            var entry: ZipEntry? = inputStream.nextEntry
                            while (entry != null) {
                                if (entry.name.startsWith(ASSETS_LOCATION)) {
                                    outputStream.putNextEntry(entry)
                                    outputStream.write(inputStream.readAllBytes())
                                    outputStream.closeEntry()
                                }
                                inputStream.closeEntry()
                                entry = inputStream.nextEntry
                            }
                        }
                    }
                }
                info("Unzip textures...")
                JarFile(file).use {
                    it.entries().toList().forEachAsync { s ->
                        if (!s.name.startsWith(ASSETS_LOCATION)) return@forEachAsync
                        val sub = s.name.substring(ASSETS_LOCATION.length)
                        if (ConfigManagerImpl.includedMinecraftTextures.any { t ->
                                sub.startsWith(t)
                            }) {
                            val split = sub.split('.')
                            fun add(name: String) {
                                it.getInputStream(s).buffered().toImage().removeEmptySide()?.let { image ->
                                    assetsMap.add(MinecraftAsset(name, image.image.width, image.image.height))
                                }
                            }
                            if (split.size == 2 && split[1] == "png") {
                                val n = split[0].substringAfterLast('_')
                                n.toIntOrNull()?.let { i ->
                                    if (i == 0) add(split[0])
                                    Unit
                                } ?: add(split[0])
                            }
                        }
                    }
                }
            }.onFailure {
                it.handle(info.sender, "Unable to load minecraft default textures.")
            }
        } else assetsMap.clear()
    }

    override fun end() {
    }
}
