package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.TextManagerImpl
import kr.toxicity.hud.text.HudText
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.text.DecimalFormat

class TextLayout(
    s: String,
    yamlObject: YamlObject,
    loc: PixelLocation
) : HudLayout(loc, yamlObject) {
    val pattern: String = yamlObject.get("pattern")?.asString().ifNull("pattern value not set: $s")
    val text: HudText = yamlObject.get("name")?.asString().ifNull("name value not set: $s").let { n ->
        TextManagerImpl.getText(n).ifNull("this text doesn't exist: $n")
    }
    val scale: Double = yamlObject.getAsDouble("scale", 1.0)
    val space: Int = yamlObject.getAsInt("space", 0).coerceAtLeast(0)
    val align: LayoutAlign = yamlObject.get("align")?.asString().toLayoutAlign()
    val lineAlign: LayoutAlign = yamlObject.get("line-align")?.asString().toLayoutAlign()
    val color: TextColor = yamlObject.get("color")?.asString()?.toTextColor() ?: NamedTextColor.WHITE
    val numberEquation: TEquation = yamlObject.get("number-equation")?.asString()?.let {
        TEquation(it)
    } ?: TEquation.t
    val numberFormat: DecimalFormat = yamlObject.get("number-format")?.asString()?.let {
        DecimalFormat(it)
    } ?: ConfigManagerImpl.numberFormat
    val disableNumberFormat: Boolean = yamlObject.getAsBoolean("disable-number-format", true)
//    val background: HudBackground? = yamlObject.get("background")?.asString()?.let {
//        BackgroundManager.getBackground(it)
//    }
//    val backgroundScale: Double = yamlObject.getAsDouble("background-scale", scale)
    val emojiLocation: PixelLocation = yamlObject.get("emoji-pixel")?.asObject()?.let {
        PixelLocation(it)
    } ?: PixelLocation.zero
    val emojiScale: Double = yamlObject.getAsDouble("emoji-scale", 1.0).apply {
        if (this <= 0) throw RuntimeException("emoji-scale cannot be <= 0")
    }
    val useLegacyFormat: Boolean = yamlObject.getAsBoolean("use-legacy-format", ConfigManagerImpl.useLegacyFormat)
    val legacySerializer: ComponentDeserializer = yamlObject.get("legacy-serializer")?.asString()?.toLegacySerializer() ?: ConfigManagerImpl.legacySerializer

    val line = yamlObject.getAsInt("line", 1).apply {
        if (this < 1) throw RuntimeException("line cannot be < 1: $s")
    }
    val splitWidth = yamlObject.getAsInt("split-width", 200).apply {
        if (this < 1) throw RuntimeException("split-width cannot be < 1: $s")
    }
    val lineWidth = yamlObject.getAsInt("line-width", 10)

    fun startJson() = jsonArrayOf(
        jsonObjectOf(
            "type" to "space",
            "advances" to buildJsonObject {
                addProperty(" ", 4)
                if (space != 0) addProperty(TEXT_SPACE_KEY_CODEPOINT.parseChar(), space)
            }
        )
    )
}