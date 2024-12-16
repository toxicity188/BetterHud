package kr.toxicity.hud.manager

import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.text.ImageTextScale
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.jar.JarFile

object MinecraftManager : BetterHudManager {

    private const val ASSETS_LOCATION = "assets/minecraft/textures/"

    private val assetsMap = Collections.synchronizedSet(HashSet<MinecraftAsset>())

    fun applyAll(layout: TextLayout, intGetter: () -> Int): Map<Int, ImageTextScale> {
        val map = HashMap<Int, ImageTextScale>()
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

    private var previous = ""

    override fun reload(sender: Audience, resource: GlobalResource) {
        if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
            val current = if (ConfigManagerImpl.minecraftJarVersion == "bukkit") BOOTSTRAP.minecraftVersion() else ConfigManagerImpl.minecraftJarVersion
            if (assetsMap.isEmpty() || previous != current) {
                previous = current
            } else return
            assetsMap.clear()
            val cache = DATA_FOLDER.subFolder(".cache")
            runWithExceptionHandling(sender, "Unable to load minecraft default textures.") {
                val client = HttpClient.newHttpClient()
                info("Getting minecraft default version...")
                val json = InputStreamReader(client.send(HttpRequest.newBuilder()
                    .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                    parseJson(it)
                }.asJsonObject
                info("Current minecraft version: $current")
                val file = File(cache, "$current.jar")
                if (!file.exists() || file.length() == 0L) {
                    info("$current.jar doesn't exist. so download it...")
                    file.outputStream().buffered().use { outputStream ->
                        client.send(HttpRequest.newBuilder()
                            .uri(URI.create(InputStreamReader(client.send(HttpRequest.newBuilder()
                                .uri(URI.create(json.getAsJsonArray("versions").map {
                                    it.asJsonObject
                                }.first {
                                    it.getAsJsonPrimitive("id").asString == current
                                }.getAsJsonPrimitive("url").asString))
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                                parseJson(it)
                            }.asJsonObject
                                .getAsJsonObject("downloads")
                                .getAsJsonObject("client")
                                .getAsJsonPrimitive("url")
                                .asString))
                            .GET().build(), HttpResponse.BodyHandlers.ofInputStream()).body().buffered().use { inputStream ->
                            inputStream.copyTo(outputStream)
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

            }
        } else assetsMap.clear()
    }

    override fun end() {
    }
}
