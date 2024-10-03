package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.layout.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor

class HeadRenderer(
    private val space: String,
    private val nextPage: String,
    private val components: List<String>,
    private val font: Key,
    private val pixel: Int,
    private val x: Int,
    private val align: LayoutAlign,
    follow: String?,
    private val cancelIfFollowerNotExists: Boolean,
    private val conditions: ConditionBuilder,
) {

    private val pixelGetter: (Int, TextColor) -> ComponentLike = if (!BOOTSTRAP.useLegacyFont()) {
        { index, next ->
            Component.text()
                .content(if (index < 63) buildString {
                    append(components[index / 8])
                    append(if (index % 8 == 7) nextPage else space)
                } else components[index / 8])
                .color(next)
        }
    } else {
        { index, next ->
            Component.text()
                .content(components[index / 8])
                .color(next)
                .append((if (index < 63) NEGATIVE_ONE_SPACE_COMPONENT else (-pixel - 1).toSpaceComponent()).component)
        }
    }

    private val followPlayer = follow?.let {
        PlaceholderManagerImpl.find(it).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }
    fun getHead(event: UpdateEvent): (HudPlayer) -> PixelComponent {
        val cond = conditions.build(event)
        val playerPlaceholder = followPlayer?.build(event)
        return build@{ player ->
            var targetPlayer = player
            var targetPlayerHead: HudPlayerHead = player.head
            playerPlaceholder?.let {
                val value = it.value(player)
                val pair = getHead(value.toString())
                pair.first?.let { player ->
                    targetPlayer = player
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
                targetPlayerHead = pair.second
            }
            if (cond(targetPlayer)) {
                val comp = Component.text().font(font)
                var i = 0
                targetPlayerHead.colors.forEachSync { next ->
                    val index = i++
                    comp.append(pixelGetter(index, next))
                }
                WidthComponent(comp, pixel).toPixelComponent(
                    when (align) {
                        LayoutAlign.LEFT -> x
                        LayoutAlign.CENTER -> x - pixel / 2
                        LayoutAlign.RIGHT -> x - pixel
                    }
                )
            } else EMPTY_PIXEL_COMPONENT
        }
    }

    private fun getHead(placeholderValue: String): Pair<HudPlayer?, HudPlayerHead> {
        return PlayerManagerImpl.getHudPlayer(placeholderValue) to PlayerHeadManager.provideHead(placeholderValue)
    }
}