package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.BackgroundManager
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.MinecraftManager
import kr.toxicity.hud.manager.TextManagerImpl
import kr.toxicity.hud.element.TextElement
import kr.toxicity.hud.text.ImageCharWidth
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.text.DecimalFormat

interface TextLayout : HudLayout<TextElement> {

    companion object {
        private fun interface EmojiProvider : (TextLayout, () -> Int) -> Map<Int, ImageCharWidth>
        private val emojiProviderMap: List<EmojiProvider> = listOf(
            EmojiProvider { layout, getter ->
                if (ConfigManagerImpl.loadMinecraftDefaultTextures) {
                    MinecraftManager.applyAll(layout, getter)
                } else emptyMap()
            }
        )
    }

    val pattern: String
    val scale: Double
    val space: Int
    val align: LayoutAlign
    val lineAlign: LayoutAlign
    val color: TextColor
    val numberEquation: TEquation
    val numberFormat: DecimalFormat
    val disableNumberFormat: Boolean
    val background: HudBackground?
    val backgroundScale: Double
    val emojiLocation: PixelLocation
    val emojiScale: Double
    val useLegacyFormat: Boolean
    val legacySerializer: ComponentDeserializer
    val line: Int
    val splitWidth: Int
    val lineWidth: Int

    fun startJson() = jsonArrayOf(
        jsonObjectOf(
            "type" to "space",
            "advances" to buildJsonObject {
                addProperty(" ", 4)
                if (space != 0) addProperty(TEXT_SPACE_KEY_CODEPOINT.parseChar(), space)
            }
        )
    )

    val imageCharMap: Map<Int, ImageCharWidth>
        get() = run {
            val map = source.imageCharWidth.toMutableMap()
            var baseValue = TEXT_IMAGE_START_CODEPOINT + map.size
            val getter = {
                ++baseValue
            }
            emojiProviderMap.forEach {
                map += it(this@TextLayout, getter)
            }
            map
        }

    class Impl(
        s: String,
        override val source: TextElement,
        group: LayoutGroup,
        yamlObject: YamlObject,
        loc: PixelLocation
    ) : TextLayout, HudLayout<TextElement> by HudLayout.Impl(source, group, loc, yamlObject) {
        constructor(
            s: String,
            group: LayoutGroup,
            yamlObject: YamlObject,
            loc: PixelLocation
        ): this(
            s,
            yamlObject["name"]?.asString().ifNull("name value not set: $s").let { n ->
                TextManagerImpl.getText(n).ifNull("this text doesn't exist: $n")
            },
            group,
            yamlObject,
            loc
        )

        override val pattern: String = yamlObject["pattern"]?.asString().ifNull("pattern value not set: $s")
        override val scale: Double = yamlObject.getAsDouble("scale", 1.0)
        override val space: Int = yamlObject.getAsInt("space", 0).coerceAtLeast(0)
        override val align: LayoutAlign = yamlObject["align"]?.asString().toLayoutAlign()
        override val lineAlign: LayoutAlign = yamlObject["line-align"]?.asString().toLayoutAlign()
        override val color: TextColor = yamlObject["color"]?.asString()?.toTextColor() ?: NamedTextColor.WHITE
        override val numberEquation: TEquation = yamlObject["number-equation"]?.asString()?.let {
            TEquation(it)
        } ?: TEquation.t
        override val numberFormat: DecimalFormat = yamlObject["number-format"]?.asString()?.let {
            DecimalFormat(it)
        } ?: ConfigManagerImpl.numberFormat
        override val disableNumberFormat: Boolean = yamlObject.getAsBoolean("disable-number-format", true)
        override val background: HudBackground? = yamlObject["background"]?.asString()?.let {
            BackgroundManager.getBackground(it)
        }
        override val backgroundScale: Double = yamlObject.getAsDouble("background-scale", scale)
        override val emojiLocation: PixelLocation = yamlObject["emoji-pixel"]?.asObject()?.let {
            PixelLocation(it)
        } ?: PixelLocation.zero
        override val emojiScale: Double = yamlObject.getAsDouble("emoji-scale", 1.0).apply {
            if (this <= 0) throw RuntimeException("emoji-scale cannot be <= 0")
        }
        override val useLegacyFormat: Boolean = yamlObject.getAsBoolean("use-legacy-format", ConfigManagerImpl.useLegacyFormat)
        override val legacySerializer: ComponentDeserializer = yamlObject["legacy-serializer"]?.asString()?.toLegacySerializer() ?: ConfigManagerImpl.legacySerializer

        override val line = yamlObject.getAsInt("line", 1).apply {
            if (this < 1) throw RuntimeException("line cannot be < 1: $s")
        }
        override val splitWidth = if (line == 1) Int.MAX_VALUE else yamlObject.getAsInt("split-width", 200).apply {
            if (this < 1) throw RuntimeException("split-width cannot be < 1: $s")
        }
        override val lineWidth = yamlObject.getAsInt("line-width", 10)
    }
}