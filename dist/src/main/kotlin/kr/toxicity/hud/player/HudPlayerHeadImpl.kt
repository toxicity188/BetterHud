package kr.toxicity.hud.player

import com.google.gson.JsonParser
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.util.textures
import kr.toxicity.hud.util.toImage
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

class HudPlayerHeadImpl(player: Player): HudPlayerHead {
    companion object {
        private val allBlack = (0..63).map {
            NamedTextColor.BLACK
        }
    }

    private val colorList = runCatching {
        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create(JsonParser.parseString(String(Base64.getDecoder().decode(player.textures)))
                    .asJsonObject
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .getAsJsonPrimitive("url")
                    .asString))
                .GET()
                .build(),
            BodyHandlers.ofInputStream()
        ).body().buffered().use {
            val ready = it.toImage()
            val image = ready.getSubimage(8, 8, 8, 8)
            val layer = ready.getSubimage(40, 8, 8, 8)
            (0..63).map { i ->
                val layerColor = layer.getRGB(i % 8, i / 8)
                val imageColor = image.getRGB(i % 8, i / 8)
                TextColor.color(if (layerColor ushr 24 != 0) layerColor else imageColor)
            }
        }
    }.getOrNull() ?: allBlack
    override fun getColors(): List<TextColor> = colorList
}