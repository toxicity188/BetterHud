package kr.toxicity.hud.layout

import kr.toxicity.hud.background.HudBackground
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.text.HudText
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
    val deserializeText: Boolean,
    val numberEquation: TEquation,
    val numberFormat: DecimalFormat,
    val disableNumberFormat: Boolean,
    val background: HudBackground?,
    val backgroundScale: Double,
    val follow: String?,
    val emojiLocation: ImageLocation,
    val emojiScale: Double,
    val conditions: ConditionBuilder
)