package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.ImageComponent
import kr.toxicity.hud.layout.ImageLayout
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

class ImageRenderer(
    layout: ImageLayout,
    component: ImageComponent
) : ImageLayout by layout, HudRenderer {
    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it, this).assertString("This placeholder is not a string: $it")
    }
    private val component = component.apply(outline, color)

    override fun render(event: UpdateEvent): TickProvider<HudPlayer, PixelComponent> {
        val cond = conditions build event
        val listen = component.listener(event)
        val follow = followHudPlayer?.build(event)

        val stackGetter = stack?.build(event)
        val maxStackGetter = maxStack?.build(event)

        val mapper = component mapper event
        val colorApply = colorOverrides(event)

        val stateMap = java.util.Collections.synchronizedMap(java.util.WeakHashMap<HudPlayer, Pair<Boolean, Long>>())

        return tickProvide(tick) build@ { player, frame ->
            val selected = mapper(player)

            val stackFrame = (stackGetter?.value(player) as? Number)?.toDouble() ?: 0.0
            val maxStackFrame = (maxStackGetter?.value(player) as? Number)?.toInt()?.coerceAtLeast(1) ?: ceil(stackFrame).toInt()

            var target = player
            follow?.let {
                PlayerManagerImpl.getHudPlayer(it.value(player).toString())?.let { p ->
                    target = p
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
            }
            
            val currentCond = cond(target)
            val currentState = stateMap[player] ?: (false to -1L)
            var lastCond = currentState.first
            var startFrame = currentState.second

            if (currentCond && !lastCond) {
                startFrame = frame
            }
            lastCond = currentCond
            stateMap[player] = lastCond to startFrame

            val adjustedFrame = if (startFrame != -1L) frame - startFrame else frame

            if (currentCond) {
                if (maxStackFrame > 1) {
                    if (stackFrame <= 0.0) return@build EMPTY_PIXEL_COMPONENT
                    var empty = EMPTY_PIXEL_COMPONENT
                    val range = 0..<maxStackFrame
                    for (i in if (reversed) range.reversed() else range) {
                        val f = ((stackFrame - i - 0.1) * selected.images.size)
                            .roundToInt()
                            .coerceAtLeast(0)
                            .coerceAtMost(selected.images.lastIndex)
                        empty = empty.append(space, selected.images[f])
                    }
                    empty.applyColor(colorApply(target))
                } else component.type.getComponent(listen, adjustedFrame, selected, target).applyColor(colorApply(target))
            } else {
                if (clearListener) listen.clear(player)
                EMPTY_PIXEL_COMPONENT
            }
        }
    }

    fun max() = component.max
}
