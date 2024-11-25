package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT

class HudElement(
    hud: HudImpl,
    resource: GlobalResource,
    private val layout: LayoutGroup,
    gui: GuiLocation,
    pixel: PixelLocation
) {
    private val imageElement = layout.image.map { image ->
        HudImageElement(hud, image, gui, pixel)
    }
    private val textElement = layout.text.map { textLayout ->
        HudTextElement(hud, resource, textLayout, gui, pixel)
    }
    private val headElement = layout.head.map { image ->
        HudHeadElement(hud, image, gui, pixel)
    }

    val conditions = layout.conditions build UpdateEvent.EMPTY

    private val max = imageElement.maxOfOrNull {
        it.max
    } ?: 0

    fun getComponent(hudPlayer: HudPlayer) = if (conditions(hudPlayer)) LayoutComponentContainer(layout.offset, layout.align, max)
        .append(imageElement.map {
            it.getComponent(hudPlayer)
        })
        .append(textElement.map {
            it.getText(hudPlayer)
        })
        .append(headElement.map {
            it.getHead(hudPlayer)
        })
        .build() else EMPTY_WIDTH_COMPONENT
}