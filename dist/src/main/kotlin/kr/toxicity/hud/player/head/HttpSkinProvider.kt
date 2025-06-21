package kr.toxicity.hud.player.head

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.util.httpClient
import kr.toxicity.hud.util.parseJson
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HttpSkinProvider : PlayerSkinProvider {
    override fun provide(player: HudPlayer): String? {
        return provideFromUUID(player.uuid().toString())
    }

    override fun provide(playerName: String): String? {
        return getUUID(playerName)?.let {
            provideFromUUID(it)
        }
    }


    private fun getUUID(playerName: String): String? {
        return httpClient {
            InputStreamReader(send(HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/$playerName"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                parseJson(it)
            }.asJsonObject.getAsJsonPrimitive("id").asString
        }.getOrNull()
    }

    private fun provideFromUUID(uuid: String): String? {
        return httpClient {
            InputStreamReader(send(HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/$uuid"))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream()).body()).buffered().use {
                parseJson(it)
            }.asJsonObject
                .getAsJsonArray("properties")
                .get(0)
                .asJsonObject
                .getAsJsonPrimitive("value")
                .asString
        }.getOrNull()
    }
}