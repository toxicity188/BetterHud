package kr.toxicity.hud.manager

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.jar.JarFile
import kotlin.math.roundToInt

object MinecraftManager : BetterHudManager {

    private const val ASSETS_LOCATION = "assets/minecraft/textures/"

    private val assetsMap = Collections.synchronizedSet(HashSet<MinecraftAsset>())

    fun applyAll(json: JsonArray, ascent: Int, scale: Double, font: Key, intGetter: () -> Int): Map<String, WidthComponent> {
        val map = HashMap<String, WidthComponent>()
        assetsMap.forEach {
            map[it.namespace.replace('/', '_')] = it.toJson(json, intGetter().parseChar(), ascent, scale, font)
        }
        return map
    }

    private class MinecraftAsset(val namespace: String, val width: Int, val height: Int) {
        fun toJson(json: JsonArray, char: String, ascent: Int, scale: Double, font: Key): WidthComponent {
            val newHeight = (height.toDouble() * scale).roundToInt()
            val newWidth = (width.toDouble() / height.toDouble() * newHeight).roundToInt()
            json.add(jsonObjectOf(
                "type" to "bitmap",
                "file" to "minecraft:$namespace.png",
                "ascent" to ascent,
                "height" to newHeight,
                "chars" to jsonArrayOf(char)
            ))
            return WidthComponent(Component.text().content(char).font(font).append(NEGATIVE_ONE_SPACE_COMPONENT.component), newWidth)
        }
    }

    override fun start() {
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
            val cache = DATA_FOLDER.subFolder(".cache")
            runWithExceptionHandling(sender, "Unable to load minecraft default textures.") {
                val client = HttpClient.newHttpClient()
                info("Getting minecraft default version...")
                val json = InputStreamReader(client.send(HttpRequest.newBuilder()
                    .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                    JsonParser.parseReader(it)
                }.asJsonObject
                val current = if (ConfigManagerImpl.minecraftJarVersion == "bukkit") BOOTSTRAP.minecraftVersion() else ConfigManagerImpl.minecraftJarVersion
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
                                JsonParser.parseReader(it)
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
        }
    }

    override fun end() {
    }
}