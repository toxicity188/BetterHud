package kr.toxicity.hud.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.LiteralUtils
import ch.njol.util.Kleenean
import kr.toxicity.hud.api.event.CustomPopupEvent
import kr.toxicity.hud.util.call
import org.bukkit.entity.Player
import org.bukkit.event.Event

class EffCallPopupEvent: Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "call popup event for ${player.toString(p0, p1)} named ${name.toString(p0, p1)} with variable of ${objects?.toString(p0, p1)} keyed by ${key?.toString(p0, p1)}"
    }

    private lateinit var player: Expression<Player>
    private lateinit var name: Expression<String>
    private var objects: Expression<*>? = null
    private var key: Expression<*>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>?>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        player = p0[0] as Expression<Player>
        name = p0[1] as Expression<String>
        if (p0[2] != null) {
            objects = p0[2] as Expression<String>
            if (p0[3] != null) key = p0[3]
            if (LiteralUtils.hasUnparsedLiteral(objects)) objects = LiteralUtils.defendExpression<Any>(objects)
        }
        return true
    }

    override fun execute(p0: Event) {
        val obj = objects?.getAll(p0) ?: emptyArray()
        name.getSingle(p0)?.let { n ->
            player.getAll(p0).forEach { p ->
                CustomPopupEvent(p, n).apply {
                    obj.forEachIndexed { i, s ->
                        variables["skript_${i + 1}"] = s.toString()
                    }
                }.call()
            }
        }

    }
}