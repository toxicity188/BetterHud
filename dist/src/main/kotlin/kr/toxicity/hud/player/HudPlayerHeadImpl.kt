package kr.toxicity.hud.player

import com.google.gson.JsonParser
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.util.gameProfile
import kr.toxicity.hud.util.toImage
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

class HudPlayerHeadImpl(player: Player): HudPlayerHead {
    private val array = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder()
            .uri(URI.create(JsonParser.parseString(String(Base64.getDecoder().decode(player.gameProfile.properties.get("textures").first().value)))
                .asJsonObject
                .getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .getAsJsonPrimitive("url")
                .asString))
            .GET()
            .build(),
        BodyHandlers.ofInputStream()
    ).body().buffered().use {
        val image = it.toImage().getSubimage(8, 8, 8, 8)
        (0..63).map { i ->
            TextColor.color(image.getRGB(i % 8, i / 8) and 0xFFFFFF)
        }
    }
}