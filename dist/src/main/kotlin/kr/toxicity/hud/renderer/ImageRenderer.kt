package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageType
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.NEGATIVE_ONE_SPACE_COMPONENT
import kr.toxicity.hud.util.NEW_LAYER

class ImageRenderer(
    private val image: HudImage,
    private val components: List<WidthComponent>,
    private val conditions: (HudPlayer) -> Boolean
) {
    private val type: ImageType = image.type
    fun getComponent(player: HudPlayer): WidthComponent {
        return if (conditions(player)) type.getComponent(image, components, player) + NEGATIVE_ONE_SPACE_COMPONENT + NEW_LAYER else EMPTY_WIDTH_COMPONENT
    }
}