package kr.toxicity.hud.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.vdurmont.semver4j.Semver
import kr.toxicity.hud.api.version.MinecraftVersion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val VERSION_GSON = GsonBuilder()
    .registerTypeAdapter(MinecraftVersion::class.java, object : JsonDeserializer<MinecraftVersion> {
        override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): MinecraftVersion =
            MinecraftVersion(p0.asString)
    })
    .registerTypeAdapter(Semver::class.java, object : JsonDeserializer<Semver> {
        override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): Semver =
            p0.asString.toSemver()
    })
    .create()

const val VERSION_CHECK_PERMISSION = "betterhud.info"

fun handleLatestVersion(): List<Component> {
    val bootstrap = BOOTSTRAP.version().toSemver()
    val list = mutableListOf<Component>()
    val latest = latestVersion()
    if (PLUGIN.isDevVersion) warn("This build is dev version - be careful to use it!")
    latest.release?.let {
        if (bootstrap < it.versionNumber)
            list += Component.text("Found a new release of BetterHud: ").append(it.toURLComponent())
    }
    latest.snapshot?.let {
        if (bootstrap < it.versionNumber)
            list += Component.text("Found a new snapshot of BetterHud: ").append(it.toURLComponent())
    }
    return list
}

fun latestVersion(): LatestVersion {
    val bootstrap = BOOTSTRAP
    return latestVersion(bootstrap.minecraftVersion(), bootstrap.loaderName())
}

fun latestVersion(minecraftVersion: MinecraftVersion, loader: String) = LatestVersion(artifactVersions(minecraftVersion, loader))

fun artifactVersions(minecraftVersion: MinecraftVersion, loader: String) = httpClient {
    send(
        HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("https://api.modrinth.com/v2/project/betterhud2/version"))
            .build(),
        HttpResponse.BodyHandlers.ofInputStream()
    ).body().use {
        InputStreamReader(it).use { reader ->
            parseJson(reader)
        }.asJsonArray
            .asSequence()
            .mapNotNull {
                runCatching {
                    VERSION_GSON.fromJson(it, ArtifactVersion::class.java)
                }.getOrNull()
            }
            .filter {
                it.gameVersions.contains(minecraftVersion) && it.loaders.contains(loader)
            }
            .sortedDescending()
            .toList()

    }
}.getOrElse {
    it.handle("Unable to BetterHud's latest version.")
    emptyList()
}

data class LatestVersion(
    val release: ArtifactVersion?,
    val snapshot: ArtifactVersion?
) {
    constructor(list: List<ArtifactVersion>): this(
        list.filter {
            it.versionType == "release"
        }.maxOrNull(),
        list.filter {
            it.versionType != "release"
        }.maxOrNull()
    )
}

data class ArtifactVersion(
    val id: String,
    @SerializedName("version_number") val versionNumber: Semver,
    @SerializedName("version_type") val versionType: String,
    @SerializedName("game_versions") val gameVersions: List<MinecraftVersion>,
    val loaders: List<String>
) : Comparable<ArtifactVersion> {
    override fun compareTo(other: ArtifactVersion): Int {
        return versionNumber.compareTo(other.versionNumber)
    }

    fun toURLComponent(): Component {
        val url = "https://modrinth.com/plugin/betterhud2/version/$id"
        return Component.text()
            .content(versionNumber.originalValue)
            .color(NamedTextColor.AQUA)
            .hoverEvent(HoverEvent.showText(
                Component.text()
                    .append(Component.text(url).color(NamedTextColor.DARK_AQUA))
                    .appendNewline()
                    .append(Component.text("Click to open download URI."))
            ))
            .clickEvent(ClickEvent.openUrl(url))
            .build()
    }
}