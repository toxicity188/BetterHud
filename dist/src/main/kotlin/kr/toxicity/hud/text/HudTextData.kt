package kr.toxicity.hud.text

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.layout.BackgroundLayout
import net.kyori.adventure.key.Key

class HudTextData(
    val word: Key,

    val images: Map<String, WidthComponent>,
    val background: BackgroundLayout?
)