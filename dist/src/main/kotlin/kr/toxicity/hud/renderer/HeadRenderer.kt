package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.tickProvide
import kr.toxicity.hud.layout.HeadLayout
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType.*
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

class HeadRenderer(
    layout: HeadLayout,
    private val space: String,
    private val nextPage: String,
    private val negativePixel: String,
    private val components: List<HeadKey>,
    private val font: Key,
    private val pixel: Int,
    private val x: Int,
) : HeadLayout by layout, HudRenderer {
    private interface HeadPixelGetter {
        fun render(head: HudPlayerHead, color: TextColor?): TextComponent.Builder
    }

    private inner class StandardPixelGetter : HeadPixelGetter {
        private val pixelGetter: (Int, TextColor) -> ComponentLike = { index, next ->
            Component.text()
                .content(buildString {
                    append(components[index / 8].bodyKey)
                    append(if (index < 63 && index % 8 == 7) nextPage else space)
                })
                .color(next)
        }
        override fun render(head: HudPlayerHead, color: TextColor?): TextComponent.Builder {
            val comp = Component.text().font(font)
            var i = 0
            head.flatHead().forEach { next ->
                val index = i++
                comp.append(pixelGetter(index, color?.let {
                    next * it
                } ?: next))
            }
            return comp
        }
    }

    private inner class FancyPixelGetter : HeadPixelGetter {
        private val headGetter: (Int, TextColor) -> ComponentLike = { index, next ->
            Component.text()
                .content(buildString {
                    append(components[index / 8].bodyKey)
                    append(negativePixel)
                })
                .color(next)
        }
        private val pixelGetter: (Int, TextColor, Boolean) -> ComponentLike = { index, next, isHair ->
            Component.text()
                .content(buildString {
                    append(if (isHair) components[index / 8].hairKey else components[index / 8].bodyKey)
                    append(if (index < 63 && index % 8 == 7) nextPage else space)
                })
                .color(next)
        }
        override fun render(head: HudPlayerHead, color: TextColor?): TextComponent.Builder {
            val comp = Component.text().font(font)
            val main = head.mainHead()
            val hair = head.hairHead()
            for (index in 0..63) {
                val next = color?.let {
                    main[index] * it
                } ?: main[index]
                hair[index]?.let {
                    comp.append(headGetter(index, next))
                        .append(pixelGetter(index, it, true))
                } ?: comp.append(pixelGetter(index, next, false))

            }
            return comp
        }
    }

    private val pixelType = when (type) {
        STANDARD -> StandardPixelGetter()
        FANCY -> FancyPixelGetter()
    }


    private val followPlayer = follow?.let {
        PlaceholderManagerImpl.find(it, this).apply {
            if (!java.lang.String::class.java.isAssignableFrom(clazz)) throw RuntimeException("This placeholder is not a string: $it")
        }
    }
    override fun render(event: UpdateEvent): TickProvider<HudPlayer, PixelComponent> {
        val cond = conditions build event
        val playerPlaceholder = followPlayer?.build(event)
        val colorApply = colorOverrides(event)
        return tickProvide(tick) build@ { player, _ ->
            var targetPlayer = player
            var targetPlayerHead: HudPlayerHead = player.head
            playerPlaceholder?.let {
                val value = it.value(player)
                val (follow, head) = getHead(value.toString())
                follow?.let { player ->
                    targetPlayer = player
                } ?: run {
                    if (cancelIfFollowerNotExists) return@build EMPTY_PIXEL_COMPONENT
                }
                targetPlayerHead = head
            }
            if (cond(targetPlayer)) {
                WidthComponent(pixelType.render(targetPlayerHead, colorApply(targetPlayer)), pixel).toPixelComponent(
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