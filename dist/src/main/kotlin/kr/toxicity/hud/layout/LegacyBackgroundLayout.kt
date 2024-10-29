package kr.toxicity.hud.layout

import kr.toxicity.hud.api.component.WidthComponent

@Deprecated(message = "Rewrite new background")
class LegacyBackgroundLayout(
    val x: Int,

    val left: WidthComponent,
    val right: WidthComponent,
    val body: WidthComponent
)