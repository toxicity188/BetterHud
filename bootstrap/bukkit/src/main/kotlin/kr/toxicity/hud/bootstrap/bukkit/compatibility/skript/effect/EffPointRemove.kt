package kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import kr.toxicity.hud.manager.PlayerManagerImpl
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EffPointRemove : Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "point remove ${name.toString(p0, p1)} to ${player.toString(p0, p1)}"
    }

    private lateinit var player: Expression<Player>
    private lateinit var name: Expression<String>

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        name = p0[0] as Expression<String>
        player = p0[1] as Expression<Player>
        return true
    }

    override fun execute(p0: Event) {
        val n = name.getSingle(p0) ?: return
        (PlayerManagerImpl.getHudPlayer((player.getSingle(p0) ?: return).uniqueId) ?: return).pointers().removeIf {
            it.name == n
        }
    }
}