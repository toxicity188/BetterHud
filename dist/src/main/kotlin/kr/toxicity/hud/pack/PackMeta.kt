package kr.toxicity.hud.pack

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.jsonArrayOf
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import kotlin.math.max

data class PackMeta(
    val pack: Pack,
    val overlays: Overlay? = Overlay()
) {
    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(VersionFormat::class.java, JsonDeserializer<VersionFormat> { src, _, _ ->
                when {
                    src.isJsonPrimitive -> VersionFormat(src.asInt)
                    src.isJsonArray -> src.asJsonArray.run {
                        if (size() < 2) VersionFormat(get(0).asInt) else VersionFormat(get(0).asInt, get(1).asInt)
                    }
                    else -> null
                }
            })
            .registerTypeAdapter(VersionFormat::class.java, JsonSerializer<VersionFormat> { src, _, _ -> src.asJson() })
            .registerTypeAdapter(VersionRange::class.java, JsonDeserializer<VersionRange> { src, _, _ ->
                when (src) {
                    is JsonObject -> VersionRange(
                        src.getAsJsonPrimitive("min_inclusive").asInt,
                        src.getAsJsonPrimitive("max_inclusive").asInt
                    )
                    is JsonPrimitive -> {
                        src.asInt.let {
                            VersionRange(it, it)
                        }
                    }
                    is JsonArray -> VersionRange(
                        src.get(0).asInt,
                        src.get(1).asInt
                    )
                    else -> null
                }
            })
            .registerTypeAdapter(VersionRange::class.java, JsonSerializer<VersionRange> { src, _, _ -> src.asJson() })
            .create()

        val default by lazy {
            PackMeta(
                Pack(
                    BOOTSTRAP.mcmetaVersion(),
                    "BetterHud's default resource pack.",
                    VersionRange(9, BOOTSTRAP.mcmetaVersion()),
                    VersionFormat(9),
                    VersionFormat(BOOTSTRAP.mcmetaVersion())
                ),
                Overlay(PackOverlay.entries.filter {
                    it.ordinal > 0
                }.map {
                    OverlayEntry(
                        VersionRange(it.minVersion, it.maxVersion),
                        it.overlayName
                    )
                })
            )
        }

        fun from(array: ByteArray): PackMeta = InputStreamReader(ByteArrayInputStream(array)).use {
            gson.fromJson(it, PackMeta::class.java)
        }
        fun from(file: File): PackMeta = file.bufferedReader().use {
            gson.fromJson(it, PackMeta::class.java)
        }

        fun VersionRange?.orDefault() = this ?: VersionRange(9, BOOTSTRAP.mcmetaVersion())
        fun VersionFormat?.or(int: Int) = this ?: VersionFormat(int)
    }

    operator fun plus(other: PackMeta): PackMeta {
        val o1 = overlays
        val o2 = other.overlays
        return PackMeta(
            pack + other.pack,
            when {
                o1 != null && o2 != null -> o1 + o2
                o1 != null -> o1
                o2 != null -> o2
                else -> null
            }
        )
    }

    fun toByteArray() = gson.toJson(this).toByteArray()

    data class Pack(
        @SerializedName("pack_format") val packFormat: Int,
        val description: String,
        @SerializedName("supported_formats") val supportedFormats: VersionRange?,
        @SerializedName("min_format") val minFormat: VersionFormat?,
        @SerializedName("max_format") val maxFormat: VersionFormat?
    ) {
        operator fun plus(other: Pack): Pack {
            return Pack(
                max(packFormat, other.packFormat),
                other.description,
                supportedFormats.orDefault() merge other.supportedFormats.orDefault(),
                minFormat.or(9) max other.minFormat.or(9),
                maxFormat.or(99) min other.maxFormat.or(99)
            )
        }
    }

    data class Overlay(
        val entries: List<OverlayEntry> = emptyList()
    ) {
        operator fun plus(other: Overlay): Overlay {
            return Overlay((entries + other.entries)
                .asSequence()
                .sorted()
                .distinctBy {
                    it.directory
                }
                .toList())
        }
    }

    data class OverlayEntry(
        val formats: VersionRange,
        val directory: String,
        @SerializedName("min_format") val minFormat: VersionFormat?,
        @SerializedName("max_format") val maxFormat: VersionFormat?
    ) : Comparable<OverlayEntry> {

        constructor(
            formats: VersionRange,
            directory: String
        ) : this (
            formats,
            directory,
            VersionFormat(formats.min),
            VersionFormat(formats.max)
        )

        override fun compareTo(other: OverlayEntry): Int {
            return formats.compareTo(other.formats)
        }
    }

    data class VersionFormat( //1.21.9
        val major: Int,
        val minor: Int
    ): Comparable<VersionFormat> {
        private companion object {
            val comparator: Comparator<VersionFormat> = compareBy<VersionFormat> {
                it.major
            }.thenComparing {
                it.minor
            }
        }

        constructor(major: Int) : this(major, 0)

        fun asJson() = if (minor <= 0) JsonPrimitive(major) else jsonArrayOf(
            major,
            minor
        )

        infix fun min(other: VersionFormat) = if (this < other) this else other
        infix fun max(other: VersionFormat) = if (this < other) other else this

        override fun compareTo(other: VersionFormat): Int = comparator.compare(this, other)
    }

    data class VersionRange(
        val min: Int,
        val max: Int
    ) : Comparable<VersionRange> {
        infix fun merge(other: VersionRange) = VersionRange(
            min.coerceAtLeast(other.min),
            max.coerceAtMost(other.max)
        )

        override fun compareTo(other: VersionRange): Int {
            return max.compareTo(other.max)
        }

        fun asJson() = if (min == max) JsonPrimitive(min) else jsonArrayOf(
            min,
            max
        )
    }
}