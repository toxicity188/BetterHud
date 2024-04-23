package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.shader.GuiLocation
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT

class HudElement(
    hud: HudImpl,
    file: List<String>,
    private val layout: LayoutGroup,
    gui: GuiLocation,
    pixel: ImageLocation
) {
    private val imageElement = layout.image.map {image ->
        HudImageElement(hud, image, gui, pixel)
    }
    private val textElement = layout.text.mapIndexed { index, textLayout ->
        HudTextElement(hud, file, textLayout, index, gui, pixel)
    }
    private val headElement = layout.head.map {image ->
        HudHeadElement(hud, image, gui, pixel)
    }

    val conditions = layout.conditions.build(UpdateEvent.EMPTY)

    private val max = imageElement.maxOfOrNull {
        it.max
    } ?: 0

    fun getComponent(player: HudPlayer) = if (conditions(player)) LayoutComponentContainer(layout.offset, layout.align, max)
        .append(imageElement.map {
            it.getComponent(player)
        })
        .append(textElement.map {
            it.getText(player)
        })
        .append(headElement.map {
            it.getHead(player)
        })
        .build() else EMPTY_WIDTH_COMPONENT
}