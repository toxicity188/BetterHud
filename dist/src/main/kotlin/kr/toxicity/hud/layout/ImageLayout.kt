package kr.toxicity.hud.layout

import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import net.kyori.adventure.text.format.TextColor

class ImageLayout(
    val image: HudImage,
    val color: TextColor,
    val location: ImageLocation,
    val scale: Double,
    val outline: Boolean,
    val layer: Int,
    val space: Int,
    val stack: PlaceholderBuilder<*>?,
    val maxStack: PlaceholderBuilder<*>?,

    val follow: String?,
    val conditions: ConditionBuilder
)