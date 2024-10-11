package kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.expression

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent

class ExprHudPlayer : SimpleExpression<Player>() {
    override fun toString(p0: Event?, p1: Boolean): String = "hud-player"
    override fun init(p0: Array<out Expression<*>>?, p1: Int, p2: Kleenean?, p3: SkriptParser.ParseResult?): Boolean = true
    override fun isSingle(): Boolean = true
    override fun getReturnType(): Class<out Player> = Player::class.java

    override fun get(p0: Event): Array<Player> {
        return if (p0 is PlayerEvent) arrayOf(p0.player) else emptyArray()
    }
}