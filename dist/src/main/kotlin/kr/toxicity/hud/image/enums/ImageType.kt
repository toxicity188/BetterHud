package kr.toxicity.hud.image.enums

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kotlin.math.roundToInt

enum class ImageType {
    SINGLE {
        override fun getComponent(listener: HudListener, frame: Int, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * list.lastIndex).roundToInt()
            }
            return if (list.isNotEmpty()) {
                if (get >= 0) list[get
                    .coerceAtLeast(0)
                    .coerceAtMost(list.lastIndex)] else list[0]
            } else EMPTY_PIXEL_COMPONENT
        }
    },
    LISTENER {
        override fun getComponent(listener: HudListener, frame: Int, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * list.lastIndex).roundToInt()
            }
            return if (get >= 0) list[get
                .coerceAtLeast(0)
                .coerceAtMost(list.lastIndex)] else list[frame % list.size]
        }
    },
    SEQUENCE {
        override fun getComponent(listener: HudListener, frame: Int, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * list.lastIndex).roundToInt()
            }
            return if (get >= 0) list[get
                .coerceAtLeast(0)
                .coerceAtMost(list.lastIndex)] else list[frame % list.size]
        }
    }
    ;

    abstract fun getComponent(listener: HudListener, frame: Int, list: List<PixelComponent>, player: HudPlayer): PixelComponent
}