package kr.toxicity.hud.image.enums

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kotlin.math.roundToInt

enum class ImageType {
    SINGLE {
        override fun getComponent(listener: HudListener, frame: Int, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (component.images.isNotEmpty()) {
                if (get >= 0) component.images[get
                    .coerceAtLeast(0)
                    .coerceAtMost(component.images.lastIndex)] else component.images[0]
            } else EMPTY_PIXEL_COMPONENT
        }
    },
    LISTENER {
        override fun getComponent(listener: HudListener, frame: Int, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (get >= 0) component.images[get
                .coerceAtLeast(0)
                .coerceAtMost(component.images.lastIndex)] else component.images[frame % component.images.size]
        }
    },
    SEQUENCE {
        override fun getComponent(listener: HudListener, frame: Int, component: ImageComponent, player: HudPlayer): PixelComponent {
            val get = listener.getValue(player).run {
                if (isNaN()) 0 else (this * component.images.lastIndex).roundToInt()
            }
            return if (get >= 0) component.images[get
                .coerceAtLeast(0)
                .coerceAtMost(component.images.lastIndex)] else component.images[frame % component.images.size]
        }
    }
    ;

    abstract fun getComponent(listener: HudListener, frame: Int, component: ImageComponent, player: HudPlayer): PixelComponent
}