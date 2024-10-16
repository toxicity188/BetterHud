package kr.toxicity.hud.image

import kr.toxicity.hud.location.GuiLocation
import kr.toxicity.hud.location.PixelLocation

data class LocationGroup(
    val gui: GuiLocation,
    val pixel: PixelLocation
)