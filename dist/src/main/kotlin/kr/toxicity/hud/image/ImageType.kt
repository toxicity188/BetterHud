package kr.toxicity.hud.image

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT

enum class ImageType {
    SINGLE {
        override fun getComponent(image: HudImage, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            return if (list.isNotEmpty()) list[0] else EMPTY_PIXEL_COMPONENT
        }
    },
    LISTENER {
        override fun getComponent(image: HudImage, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            return list[((image as ListenerHudImage).listener.getValue(player) * list.size).toInt().coerceAtLeast(0).coerceAtMost(list.lastIndex)]
        }
    },
    SEQUENCE {
        override fun getComponent(image: HudImage, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            return list[(player.tick % list.size).toInt()]
        }
    }
    ;

    abstract fun getComponent(image: HudImage, list: List<PixelComponent>, player: HudPlayer): PixelComponent
}