package kr.toxicity.hud.player.location

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.player.PointedLocationSource

class GPSLocationProvider: PointedLocationProvider {

    override fun provide(player: HudPlayer): PointedLocation? {
        return GPSWrapper.getNearestPoint(player.bukkitPlayer)?.let {
            PointedLocation(
                PointedLocationSource.GPS,
                "target_location",
                it
            )
        }
    }
}