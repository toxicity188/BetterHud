package kr.toxicity.hud.player.head

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.util.parseJson
import kr.toxicity.hud.util.toImage
import kr.toxicity.hud.util.warn
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.awt.image.BufferedImage
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

class HudPlayerHeadImpl(
    private val colorList: List<TextColor>,
    private val hairMap: Map<Int, TextColor?>
) : HudPlayerHead {
    private constructor(image: BufferedImage) : this(
        image.getSubimage(8, 8, 8, 8), image.getSubimage(40, 8, 8, 8)
    )
    private constructor(main: BufferedImage, hair: BufferedImage) : this(
        (0..63).map { i ->
            TextColor.color(main.getRGB(i % 8, i / 8))
        },
        (0..63).associateWith { i ->
            val rgb = hair.getRGB(i % 8, i / 8)
            if (rgb ushr 24 > 0) {
                TextColor.color(rgb)
            } else null
        }
    )
    private constructor(playerName: String) : this(
        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create(
                    parseJson(String(Base64.getDecoder().decode(PlayerHeadManager.provideSkin(playerName))))
                    .asJsonObject
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .getAsJsonPrimitive("url")
                    .asString
                ))
                .GET()
                .build(),
            BodyHandlers.ofInputStream()
        ).body().buffered().use {
            it.toImage()
        }
    )

    private val flatHead = (0..63).map {
        hairMap[it] ?: colorList[it]
    }

    companion object {
        val allBlack = HudPlayerHeadImpl((0..63).map {
            NamedTextColor.BLACK
        }, emptyMap())
        fun of(playerName: String) = runCatching {
            HudPlayerHeadImpl(playerName)
        }.getOrElse { e ->
            warn(
                "Unable to get ${playerName}'s head.",
                "Reason: ${e.message}"
            )
            allBlack
        }
    }

    override fun flatHead(): List<TextColor> = flatHead
    override fun mainHead(): List<TextColor> = colorList
    override fun hairHead(): Map<Int, TextColor?> = hairMap
}