package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kr.toxicity.hud.util.append
import kr.toxicity.hud.util.applyColor
import kotlin.math.ceil
import kotlin.math.roundToInt

class ImageRenderer(
    layout: ImageLayout,
    component: ImageComponent
) : ImageLayout by layout {
    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }
    private val component = component applyColor color
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int) -> PixelComponent {
        val cond = conditions build reason
        val listen = component.listener(reason)
        val follow = followHudPlayer?.build(reason)

        val stackGetter = stack?.build(reason)
        val maxStackGetter = maxStack?.build(reason)

        val mapper = component mapper reason
        val colorApply = colorOverrides(reason)

        return build@ { hudPlayer, frame ->

            val selected = mapper(hudPlayer)

            val stackFrame = (stackGetter?.value(hudPlayer) as? Number)?.toDouble() ?: 0.0
            val maxStackFrame = (maxStackGetter?.value(hudPlayer) as? Number)?.toInt()?.coerceAtLeast(1) ?: ceil(stackFrame).toInt()

            var target = hudPlayer
            follow?.let {
                PlayerManagerImpl.getHudPlayer(it.value(hudPlayer).toString())?.let { p ->
                    target = p
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
            }
            if (cond(target)) {
                if (maxStackFrame > 1) {
                    if (stackFrame <= 0.0) return@build EMPTY_PIXEL_COMPONENT
                    var empty = EMPTY_PIXEL_COMPONENT
                    for (i in 0..<maxStackFrame) {
                        empty = empty.append(space, selected.images[((stackFrame - i - 0.1) * selected.images.size)
                            .roundToInt()
                            .coerceAtLeast(0)
                            .coerceAtMost(selected.images.lastIndex)])
                    }
                    empty.applyColor(colorApply(target))
                } else component.type.getComponent(listen, frame, selected, target).applyColor(colorApply(target))
            } else EMPTY_PIXEL_COMPONENT
        }
    }

    fun max() = component.max
}