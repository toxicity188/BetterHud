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
        return InputStreamReader(HttpClient.newHttpClient().send(HttpRequest.newBuilder()
            .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/${player.uniqueId.toString().replace("_","").lowercase()}"))
            .GET()
            .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                JsonParser.parseReader(it)
        }.asJsonObject
            .getAsJsonArray("properties")
            .get(0)
            .asJsonObject
            .getAsJsonPrimitive("value")
            .asString
    }
}