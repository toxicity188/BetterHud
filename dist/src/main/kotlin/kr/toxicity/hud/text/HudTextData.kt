package kr.toxicity.hud.text

import kr.toxicity.hud.renderer.BackgroundRenderer

class HudTextData(
    val font: List<HudTextFont>,
    val splitWidth: Int,
    val background: BackgroundRenderer?
)