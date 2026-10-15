package kr.toxicity.hud.renderer.bake

import kr.toxicity.hud.animation.Animated
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.renderer.HudRenderer
import kr.toxicity.hud.util.EMPTY_PIXEL_COMPONENT
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT

class AnimatedLayout(
    host: ElementHost,
    fontDir: List<String>,
    private val layout: LayoutGroup,
    gui: GuiLocation,
    base: PixelLocation,
    layoutIndex: Int
) {
    private val bakery = ElementBakery(host, fontDir, gui, layoutIndex)

    private val images = layout.image.map { image ->
        image.animation.bake { if (it.visible) bakery.image(image, base, it) else null }
    }
    private val texts = layout.text.map { text ->
        text.animation.bake { if (it.visible) bakery.text(text, base, it) else null }
    }
    private val heads = layout.head.map { head ->
        head.animation.bake { if (it.visible) bakery.head(head, base, it) else null }
    }

    private val max = images.maxOfOrNull { animated ->
        animated.states.maxOfOrNull { it?.max() ?: 0 } ?: 0
    } ?: 0

    private val elements: List<Animated<HudRenderer?>> = images + texts + heads

    fun render(reason: UpdateEvent): (HudPlayer) -> (Long) -> WidthComponent {
        val condition = layout.conditions build reason
        val bound = elements.map { animated ->
            animated.map { it?.render(reason) }
        }
        return { player ->
            val perPlayer = bound.map { animated ->
                animated.map { it?.invoke(player) }
            }
            val byFrame: (Long) -> WidthComponent = { frame ->
                if (condition(player)) LayoutComponentContainer(layout.offset, layout.align, max)
                    .append(perPlayer.map { it.at(frame)?.invoke(frame) ?: EMPTY_PIXEL_COMPONENT })
                    .build()
                else EMPTY_WIDTH_COMPONENT
            }
            byFrame
        }
    }
}
