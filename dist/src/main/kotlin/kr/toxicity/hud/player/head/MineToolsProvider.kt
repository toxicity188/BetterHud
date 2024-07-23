package kr.toxicity.hud.player.head

import com.google.gson.JsonParser
import org.bukkit.entity.Player
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class MineToolsProvider : PlayerSkinProvider {
    override fun provide(player: Player): String? {
        return provideFromUUID(player.uniqueId.toString())
    }

    override fun provide(playerName: String): String? {
        return getUUIDFromName(playerName)?.let {
            provideFromUUID(it)
        }
    }

    private fun getUUIDFromName(playerName: String): String? {
        return runCatching {
            InputStreamReader(
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api.minetools.eu/uuid/${playerName}"))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofInputStream()
                ).body()
            ).buffered().use {
                JsonParser.parseReader(it)
            }.asJsonObject.getAsJsonPrimitive("id").asString
        }.getOrNull()
    }

    private fun provideFromUUID(uuid: String): String? {
        return runCatching {
            InputStreamReader(
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://api.minetools.eu/profile/$uuid"))
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofInputStream()
                ).body()
            ).buffered().use {
                JsonParser.parseReader(it)
            }.asJsonObject
                .getAsJsonObject("raw")
                .getAsJsonArray("properties")
                .get(0)
                .asJsonObject
                .getAsJsonPrimitive("value")
                .asString
        }.getOrNull()
    }
}