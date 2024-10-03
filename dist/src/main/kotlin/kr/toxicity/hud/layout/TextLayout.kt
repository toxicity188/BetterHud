package kr.toxicity.hud.layout

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.text.HudText
import kr.toxicity.hud.util.ComponentDeserializer
import kr.toxicity.hud.util.TEXT_SPACE_KEY_CODEPOINT
import kr.toxicity.hud.util.parseChar
import net.kyori.adventure.text.format.TextColor
import java.text.DecimalFormat

class TextLayout(
    val pattern: String,
    val text: HudText,
    val location: ImageLocation,
    val scale: Double,
    val space: Int,
    val align: LayoutAlign,
    val color: TextColor,
    val outline: Boolean,
    val layer: Int,
    val numberEquation: TEquation,
    val numberFormat: DecimalFormat,
    val disableNumberFormat: Boolean,
    val background: HudBackground?,
    val backgroundScale: Double,
    val follow: String?,
    val cancelIfFollowerNotExists: Boolean,
    val emojiLocation: ImageLocation,
    val emojiScale: Double,
    val useLegacyFormat: Boolean,
    val legacySerializer: ComponentDeserializer,
    val conditions: ConditionBuilder
) {
    fun startJson() = JsonArray().apply {
        add(JsonObject().apply {
            addProperty("type", "space")
            add("advances", JsonObject().apply {
                addProperty(" ", 4)
                if (space != 0) addProperty(TEXT_SPACE_KEY_CODEPOINT.parseChar(), space)
            })
        })
    }
}