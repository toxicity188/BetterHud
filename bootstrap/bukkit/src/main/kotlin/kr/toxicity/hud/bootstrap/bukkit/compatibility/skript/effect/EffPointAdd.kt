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

class EffPointAdd: Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "point add ${location.toString(p0, p1)} named ${name.toString(p0, p1)} with icon ${icon?.toString(p0, p1) ?: "default"} to ${player.toString(p0, p1)}"
    }

    private lateinit var player: Expression<Player>
    private lateinit var location: Expression<Location>
    private lateinit var name: Expression<String>
    private var icon: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        location = p0[0] as Expression<Location>
        name = p0[1] as Expression<String>
        icon = p0[2] as? Expression<String>
        player = p0[3] as Expression<Player>
        return true
    }

    override fun execute(p0: Event) {
        (PlayerManagerImpl.getHudPlayer((player.getSingle(p0) ?: return).uniqueId) ?: return).pointers().add(location.getSingle(p0)?.let {
            PointedLocation(
                PointedLocationSource.INTERNAL,
                name.getSingle(p0) ?: return,
                icon?.getSingle(p0),
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