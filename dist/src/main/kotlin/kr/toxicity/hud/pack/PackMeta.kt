package kr.toxicity.hud.pack

import com.google.gson.annotations.SerializedName
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.GSON
import java.io.File
import java.util.zip.ZipEntry
import kotlin.math.max

data class PackMeta(
    val pack: Pack,
    val overlays: Overlay = Overlay()
) {
    companion object {
        val default by lazy {
            PackMeta(
                Pack(
                    BOOTSTRAP.mcmetaVersion(),
                    "BetterHud's default resource pack.",
                    VersionRange(9, 55)
                )
            )
        }
        val zipEntry = ZipEntry("pack.mcmeta")

        fun fromFile(file: File): PackMeta = file.bufferedReader().use {
            GSON.fromJson(it, PackMeta::class.java)
        }
    }

    operator fun plus(other: PackMeta): PackMeta {
        return PackMeta(
            pack + other.pack,
            overlays + other.overlays
        )
    }

    fun toByteArray() = GSON.toJson(this).toByteArray()

    data class Pack(
        @SerializedName("pack_format") val packFormat: Int,
        val description: String,
        @SerializedName("supported_formats") val supportedFormats: VersionRange?
    ) {
        operator fun plus(other: Pack): Pack {
            return Pack(
                max(packFormat, other.packFormat),
                other.description,
                supportedFormats?.min(other.supportedFormats)
            )
        }
    }

    data class Overlay(
        val entries: List<OverlayEntry> = emptyList()
    ) {
        operator fun plus(other: Overlay): Overlay {
            return Overlay((entries + other.entries).distinctBy {
                it.directory
            })
        }
    }

    data class OverlayEntry(
        val formats: VersionRange,
        val directory: String
    )

    data class VersionRange(
        @SerializedName("min_inclusive") val min: Int,
        @SerializedName("max_inclusive") val max: Int
    ) {
        infix fun min(other: VersionRange?) = if (other != null) VersionRange(
            min.coerceAtMost(other.min),
            max.coerceAtMost(other.max)
        ) else this
    }
}