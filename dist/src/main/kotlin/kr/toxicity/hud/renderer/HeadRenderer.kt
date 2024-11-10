package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.HudPlayerHead
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.player.head.HeadKey
import kr.toxicity.hud.player.head.HeadRenderType
import kr.toxicity.hud.player.head.HeadRenderType.*
import kr.toxicity.hud.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

class HeadRenderer(
    private val space: String,
    private val nextPage: String,
    private val negativePixel: String,
    private val components: List<HeadKey>,
    private val font: Key,
    private val pixel: Int,
    private val x: Int,
    private val align: LayoutAlign,
    type: HeadRenderType,
    follow: String?,
    private val cancelIfFollowerNotExists: Boolean,
    private val conditions: ConditionBuilder,
) {
    private interface HeadPixelGetter {
        fun render(head: HudPlayerHead): TextComponent.Builder
    }

    private inner class StandardPixelGetter : HeadPixelGetter {
        private val pixelGetter: (Int, TextColor) -> ComponentLike = if (!BOOTSTRAP.useLegacyFont()) {
            { index, next ->
                Component.text()
                    .content(buildString {
                        append(components[index / 8].bodyKey)
                        append(if (index < 63 && index % 8 == 7) nextPage else space)
                    })
                    .color(next)
            }
        } else {
            { index, next ->
                Component.text()
                    .content(components[index / 8].bodyKey)
                    .color(next)
                    .append((if (index < 63 && index % 8 == 7) (-pixel - 1).toSpaceComponent() else NEGATIVE_ONE_SPACE_COMPONENT).component)
            }
        }
        override fun render(head: HudPlayerHead): TextComponent.Builder {
            val comp = Component.text().font(font)
            var i = 0
            head.flatHead().forEach { next ->
                val index = i++
                comp.append(pixelGetter(index, next))
            }
            return comp
        }
    }

    private inner class FancyPixelGetter : HeadPixelGetter {
        private val headGetter: (Int, TextColor) -> ComponentLike = if (!BOOTSTRAP.useLegacyFont()) {
            { index, next ->
                Component.text()
                    .content(buildString {
                        append(components[index / 8].bodyKey)
                        append(negativePixel)
                    })
                    .color(next)
            }
        } else {
            { index, next ->
                Component.text()
                    .content(components[index / 8].bodyKey)
                    .color(next)
                    .append((-pixel / 8 - 1).toSpaceComponent().component)
            }
        }
        private val pixelGetter: (Int, TextColor, Boolean) -> ComponentLike = if (!BOOTSTRAP.useLegacyFont()) {
            { index, next, isHair ->
                Component.text()
                    .content(buildString {
                        append(if (isHair) components[index / 8].hairKey else components[index / 8].bodyKey)
                        append(if (index < 63 && index % 8 == 7) nextPage else space)
                    })
                    .color(next)
            }
        } else {
            { index, next, isHair ->
                Component.text()
                    .content(if (isHair) components[index / 8].hairKey else components[index / 8].bodyKey)
                    .color(next)
                    .append((if (index < 63 && index % 8 == 7) (-pixel - 1).toSpaceComponent() else NEGATIVE_ONE_SPACE_COMPONENT).component)
            }
        }
        override fun render(head: HudPlayerHead): TextComponent.Builder {
            val comp = Component.text().font(font)
            val main = head.mainHead()
            val hair = head.hairHead()
            for (index in 0..63) {
                hair[index]?.let {
                    comp.append(headGetter(index, main[index]))
                        .append(pixelGetter(index, it, true))
                } ?: comp.append(pixelGetter(index, main[index], false))

            }
            return comp
        }
    }

    private val pixelType = when (type) {
        STANDARD -> StandardPixelGetter()
        FANCY -> FancyPixelGetter()
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
                WidthComponent(pixelType.render(targetPlayerHead), pixel).toPixelComponent(
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