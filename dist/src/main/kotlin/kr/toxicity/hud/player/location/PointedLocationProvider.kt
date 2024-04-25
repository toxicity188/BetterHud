package kr.toxicity.hud.player.location

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation

interface PointedLocationProvider {
    fun provide(player: HudPlayer): PointedLocation?
}