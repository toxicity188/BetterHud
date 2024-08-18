package kr.toxicity.hud.bootstrap.bukkit.player.location

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.player.PointedLocationProvider
import kr.toxicity.hud.api.player.PointedLocationSource
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import kr.toxicity.hud.player.location.GPSWrapper

class GPSLocationProvider: PointedLocationProvider {

    override fun provide(player: HudPlayer): PointedLocation? {
        return GPSWrapper.getNearestPoint(player.bukkitPlayer)?.let {
            PointedLocation(
                PointedLocationSource.GPS,
                "target_location",
                LocationWrapper(
                    player.world(),
                    it.x,
                    it.y,
                    it.z,
                    it.yaw,
                    it.pitch
                )
            )
        }
    }
}