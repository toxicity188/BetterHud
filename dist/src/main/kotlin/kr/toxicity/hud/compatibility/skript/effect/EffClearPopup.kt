package kr.toxicity.hud.compatibility.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import kr.toxicity.hud.manager.PlayerManagerImpl
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EffClearPopup: Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "clear popup of ${expr.toString(p0, p1)}"
    }

    private lateinit var expr: Expression<Player>

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        expr = p0[0] as Expression<Player>
        return true
    }

    override fun execute(p0: Event) {
        expr.getAll(p0).forEach {
            PlayerManagerImpl.getHudPlayer(it.uniqueId)?.let { player ->
                player.popupKeyMap.clear()
                player.popupGroupIteratorMap.clear()
            }
        }
    }
}