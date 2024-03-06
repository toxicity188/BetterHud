package kr.toxicity.hud.hud

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.component.LayoutComponentContainer
import kr.toxicity.hud.layout.LayoutGroup
import kr.toxicity.hud.util.EMPTY_WIDTH_COMPONENT
import kr.toxicity.hud.util.subFolder
import java.io.File

class HudElement(name: String, file: File, layout: LayoutGroup, x: Double, y: Double) {
    private val imageElement = run {
        val subFile = file.subFolder("image")
        layout.image.mapIndexed { index, image ->
            HudImageElement(name, subFile, image, index, x, y, layout.animation)
        }
    }
    private val textElement = run {
        val subFile = file.subFolder("text")
        layout.text.mapIndexed { index, textLayout ->
            HudTextElement(name, subFile, textLayout, index, x, y, layout.animation)
        }
    }

    val conditions = layout.conditions

    fun getComponent(player: HudPlayer) = if (conditions(player)) LayoutComponentContainer()
        .append(imageElement.map {
            it.getComponent(player)
        })
        .append(textElement.map {
            it.getText(player)
        })
        .build() else EMPTY_WIDTH_COMPONENT
}