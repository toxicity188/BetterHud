package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.image.HudImage
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.placeholder.PlaceholderBuilder
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kr.toxicity.hud.util.append
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.ceil
import kotlin.math.roundToInt

class ImageRenderer(
    image: HudImage,
    color: TextColor,
    private val space: Int,
    private val stack: PlaceholderBuilder<*>?,
    private val maxStack: PlaceholderBuilder<*>?,
    components: List<PixelComponent>,
    follow: String?,
    private val conditions: ConditionBuilder
) {
    private val type = image.type
    private val components = components.map {
        val comp = it.component
        PixelComponent(WidthComponent(Component.text().append(comp.component.build().color(color)), comp.width), it.pixel)
    }

    private val followHudPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }

    val listener: (UpdateEvent) -> HudListener = image.listener ?: HudListener.EMPTY.let {
        { _ ->
            it
        }
    }
    fun getComponent(reason: UpdateEvent): (HudPlayer, Int) -> PixelComponent {
        val cond = conditions.build(reason)
        val listen = listener(reason)
        val follow = followHudPlayer?.build(reason)

        val stackGetter = stack?.build(reason)
        val maxStackGetter = maxStack?.build(reason)

        return build@ { hudPlayer, frame ->

            val stackFrame = (stackGetter?.value(hudPlayer) as? Number)?.toDouble() ?: 0.0
            val maxStackFrame = (maxStackGetter?.value(hudPlayer) as? Number)?.toInt()?.coerceAtLeast(1) ?: ceil(stackFrame).toInt()

            var target = hudPlayer
            follow?.let {
                PlayerManagerImpl.getHudPlayer(it.value(hudPlayer).toString())?.let { p ->
                    target = p
                } ?: return@build EMPTY_PIXEL_COMPONENT
            }
            if (cond(target)) {
                if (maxStackFrame > 1) {
                    if (stackFrame <= 0.0) return@build EMPTY_PIXEL_COMPONENT
                    var empty = EMPTY_PIXEL_COMPONENT
                    for (i in 0..<maxStackFrame) {
                        empty = empty.append(space, components[((stackFrame - i - 0.1) * components.size)
                            .roundToInt()
                            .coerceAtLeast(0)
                            .coerceAtMost(components.lastIndex)])
                    }
                    empty
                } else type.getComponent(listen, frame, components, target)
            } else EMPTY_PIXEL_COMPONENT
        }
    }

    fun max() = components.maxOfOrNull {
        it.component.width
    } ?: 0
}