package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.layout.LayoutAlign
import kr.toxicity.hud.layout.TextLayout
import kr.toxicity.hud.placeholder.ConditionBuilder
import kr.toxicity.hud.util.*
import net.kyori.adventure.text.Component
import java.util.UUID

class HeadRenderer(
    private val components: List<Component>,
    private val pixel: Int,
    private val x: Int,
    private val align: LayoutAlign,
    private val conditions: ConditionBuilder,
) {
    private val componentMap = HashMap<UUID, PixelComponent>()
    private val nextPixel = (-pixel * 8).toSpaceComponent() + NEGATIVE_ONE_SPACE_COMPONENT

    fun getHead(event: UpdateEvent): (HudPlayer) -> PixelComponent {
        val cond = conditions.build(event)
        return { player ->
            if (cond(player)) componentMap.computeIfAbsent(player.bukkitPlayer.uniqueId) {
                var comp = EMPTY_WIDTH_COMPONENT
                player.head.colors.forEachIndexed { index, textColor ->
                    comp += WidthComponent(components[index / 8].color(textColor), pixel)
                    comp += if (index < 63 && index % 8 == 7) nextPixel else NEGATIVE_ONE_SPACE_COMPONENT
                }
                comp.toPixelComponent(when (align) {
                    LayoutAlign.LEFT -> x
                    LayoutAlign.CENTER -> x - comp.width / 2
                    LayoutAlign.RIGHT -> x - comp.width
                })
            } else EMPTY_PIXEL_COMPONENT
        }
    }
}