package kr.toxicity.hud.player.head

import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.awt.image.BufferedImage
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*


private fun getSkinTextureUri(textureData: String): URI {
    require(textureData.isNotBlank()) {
        "Skin texture data is empty."
    }

    val decoded = String(
        Base64.getDecoder().decode(textureData),
        Charsets.UTF_8
    )

    val skinUrl = parseJson(decoded)
        .asJsonObject
        .getAsJsonObject("textures")
        .getAsJsonObject("SKIN")
        .getAsJsonPrimitive("url")
        .asString

    val uri = URI.create(skinUrl)

    require(uri.scheme == "https" || uri.scheme == "http") {
        "Invalid skin texture protocol."
    }

    require(uri.host == "textures.minecraft.net") {
        "Unsupported skin texture host."
    }

    return uri
}

private fun loadSkinImage(textureData: String): BufferedImage {
    val uri = getSkinTextureUri(textureData)

    return httpClient {
        send(
            HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build(),
            BodyHandlers.ofInputStream()
        ).body().buffered().use {
            it.toImage()
        }
    }.getOrThrow()
}


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
        }.toIntKeyMap()
    )
    private constructor(playerName: String) : this(
        loadSkinImage(PlayerHeadManager.provideSkin(playerName))
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
            e.handle("Unable to get ${playerName}'s head.")
            allBlack
        }
        fun isTextureData(textureData: String): Boolean {
            return runCatching {
                getSkinTextureUri(textureData)
            }.isSuccess
        }

        fun fromTextureData(textureData: String): HudPlayerHeadImpl {
            return runCatching {
                HudPlayerHeadImpl(loadSkinImage(textureData))
            }.getOrElse { e ->
                e.handle("Unable to load head from texture data.")
                allBlack
            }
        }
    }

    override fun flatHead(): List<TextColor> = flatHead
    override fun mainHead(): List<TextColor> = colorList
    override fun hairHead(): Map<Int, TextColor?> = hairMap
}