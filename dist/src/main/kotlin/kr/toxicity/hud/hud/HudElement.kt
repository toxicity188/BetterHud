package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.subFolder
import java.io.File

class HudElement(hud: HudImpl, name: String, file: File, private val layout: LayoutGroup, x: Double, y: Double) {
    private val imageElement = layout.image.map {image ->
        HudImageElement(hud, image, x, y, layout.animation)
    }
    private val textElement = run {
        val subFile = file.subFolder("text")
        layout.text.mapIndexed { index, textLayout ->
            HudTextElement(hud, name, subFile, textLayout, index, x, y, layout.animation)
        }
    }

    val conditions = layout.conditions.build(UpdateEvent.EMPTY)

    private val max = imageElement.maxOf {
        it.max
    }

    fun getComponent(player: HudPlayer) = if (conditions(player)) LayoutComponentContainer(layout.align, max)
        .append(imageElement.map {
            it.getComponent(player)
        })
        .append(textElement.map {
            it.getText(player)
        })
        .build() else EMPTY_WIDTH_COMPONENT
}