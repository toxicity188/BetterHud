package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.image.ImageType
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT

class ImageRenderer(
    image: HudImage,
    private val components: List<PixelComponent>,
    private val conditions: ConditionBuilder
) {
    private val type: ImageType = image.type
    val listener: (UpdateEvent) -> HudListener = image.listener ?: HudListener.ZERO.let {
        { _ ->
            it
        }
    }
    fun getComponent(reason: UpdateEvent): (HudPlayer) -> PixelComponent {
        val cond = conditions.build(reason)
        val listen = listener(reason)
        return { player ->
            if (cond(player)) type.getComponent(listen, components, player) else EMPTY_PIXEL_COMPONENT
        }
    }

    fun max() = components.maxOfOrNull {
        it.component.width
    } ?: 0
}