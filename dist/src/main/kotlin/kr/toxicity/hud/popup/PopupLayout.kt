package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.renderer.bake.AnimatedLayout
import kr.toxicity.hud.util.Runner

class PopupLayout(
    globalIndex: Int,
    layout: LayoutGroup,
    parent: PopupImpl,
    globalLocation: GuiLocation,
    globalPixel: PixelLocation,
    file: List<String>
) {
    private val groups = parent.move.locations.map { location ->
        AnimatedLayout(
            parent,
            file,
            layout,
            location.gui + parent.gui + globalLocation,
            globalPixel + location.pixel,
            globalIndex
        )
    }

    fun getComponent(reason: UpdateEvent, frameSupplier: (HudPlayer) -> Long = { it.tick }): (HudPlayer, Int) -> Runner<WidthComponent> {
        val rendered = groups.map { it.render(reason) }
        return { player, index ->
            val byFrame = rendered[index](player)
            Runner {
                byFrame(frameSupplier(player))
            }
        }
    }
}
