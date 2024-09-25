package kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.player.PointedLocationSource
import kr.toxicity.hud.manager.PlayerManagerImpl
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EffPoint: Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "point ${player.toString(p0, p1)} to ${location.toString(p0, p1)} named ${string.toString(p0, p1)}"
    }

    private lateinit var player: Expression<Player>
    private lateinit var location: Expression<Location>
    private lateinit var string: Expression<String>

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        player = p0[0] as Expression<Player>
        location = p0[1] as Expression<Location>
        string = p0[2] as Expression<String>
        return true
    }

    override fun execute(p0: Event) {
        (PlayerManagerImpl.getHudPlayer((player.getSingle(p0) ?: return).uniqueId) ?: return).pointer(location.getSingle(p0)?.let {
            PointedLocation(
                PointedLocationSource.INTERNAL,
                string.getSingle(p0) ?: return,
                LocationWrapper(
                    it.world?.let { w ->
                        WorldWrapper(w.name, w.uid)
                    } ?: return,
                    it.x,
                    it.y,
                    it.z,
                    it.pitch,
                    it.yaw
                )
            )
        } ?: return)
    }
}