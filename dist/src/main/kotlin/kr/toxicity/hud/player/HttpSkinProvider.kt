package kr.toxicity.hud.player

import com.google.gson.JsonParser
import org.bukkit.entity.Player
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HttpSkinProvider: PlayerSkinProvider {
    override fun provide(player: Player): String? {
        return runCatching {
            val uuid = InputStreamReader(HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/${player.name}?at=${System.currentTimeMillis() / 1000}"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                JsonParser.parseReader(it)
            }.asJsonObject.getAsJsonPrimitive("id").asString
            InputStreamReader(HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/$uuid"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                JsonParser.parseReader(it)
            }.asJsonObject
                .getAsJsonArray("properties")
                .get(0)
                .asJsonObject
                .getAsJsonPrimitive("value")
                .asString
        }.getOrNull()
    }
}