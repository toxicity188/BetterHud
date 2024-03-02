package kr.toxicity.hud.image

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer

enum class ImageType {
    SINGLE {
        override fun getComponent(image: HudImage, list: List<WidthComponent>, player: HudPlayer): WidthComponent {
            return list.first()
        }
    },
    LISTENER {
        override fun getComponent(image: HudImage, list: List<WidthComponent>, player: HudPlayer): WidthComponent {
            return list[((image as ListenerHudImage).listener.getValue(player) * list.size).toInt().coerceAtLeast(0).coerceAtMost(list.lastIndex)]
        }
    },
    SEQUENCE {
        override fun getComponent(image: HudImage, list: List<WidthComponent>, player: HudPlayer): WidthComponent {
            return list[(player.tick % list.size).toInt()]
        }
    }
    ;

    abstract fun getComponent(image: HudImage, list: List<WidthComponent>, player: HudPlayer): WidthComponent
}