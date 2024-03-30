package kr.toxicity.hud.image

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kotlin.math.round
import kotlin.math.roundToInt

enum class ImageType {
    SINGLE {
        override fun getComponent(listener: HudListener, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else roundToInt()
            }
            return if (list.isNotEmpty()) {
                if (get >= 0) list[(get * list.lastIndex)
                    .coerceAtLeast(0)
                    .coerceAtMost(list.lastIndex)] else list[0]
            } else EMPTY_PIXEL_COMPONENT
        }
    },
    LISTENER {
        override fun getComponent(listener: HudListener, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else roundToInt()
            }
            return if (get >= 0) list[(get * list.lastIndex)
                .coerceAtLeast(0)
                .coerceAtMost(list.lastIndex)] else list[(player.tick % list.size).toInt()]
        }
    },
    SEQUENCE {
        override fun getComponent(listener: HudListener, list: List<PixelComponent>, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else roundToInt()
            }
            return if (get >= 0) list[(get * list.lastIndex)
                .coerceAtLeast(0)
                .coerceAtMost(list.lastIndex)] else list[(player.tick % list.size).toInt()]
        }
    }
    ;

    abstract fun getComponent(listener: HudListener, list: List<PixelComponent>, player: HudPlayer): PixelComponent
}