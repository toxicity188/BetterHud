package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageType
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.NEGATIVE_ONE_SPACE_COMPONENT
import kr.toxicity.hud.util.NEW_LAYER

class ImageRenderer(
    private val image: HudImage,
    private val components: List<PixelComponent>,
    private val conditions: (HudPlayer) -> Boolean
) {
    private val type: ImageType = image.type
    fun getComponent(player: HudPlayer): PixelComponent {
        return if (conditions(player)) type.getComponent(image, components, player) else EMPTY_PIXEL_COMPONENT
    }
}