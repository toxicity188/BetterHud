package kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.effect

import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.util.LiteralUtils
import ch.njol.util.Kleenean
import kr.toxicity.hud.api.bukkit.event.CustomPopupEvent
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent
import kr.toxicity.hud.bootstrap.bukkit.util.toHud
import kr.toxicity.hud.manager.PopupManagerImpl
import org.bukkit.entity.Player
import org.bukkit.event.Event
import java.util.*

class EffShowPopup : Effect() {
    override fun toString(p0: Event?, p1: Boolean): String {
        return "show popup ${popup.toString(p0, p1)} to ${player.toString(p0, p1)} with variable of ${objects?.toString(p0, p1)} keyed by ${key?.toString(p0, p1)}"
    }

    private lateinit var popup: Expression<String>
    private lateinit var player: Expression<Player>
    private var objects: Expression<*>? = null
    private var key: Expression<*>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(p0: Array<out Expression<*>?>, p1: Int, p2: Kleenean, p3: SkriptParser.ParseResult): Boolean {
        popup = p0[0] as Expression<String>
        player = p0[1] as Expression<Player>
        if (p0[2] != null) {
            objects = p0[2] as Expression<String>
            if (p0[3] != null) key = p0[3]
            if (LiteralUtils.hasUnparsedLiteral(objects)) objects = LiteralUtils.defendExpression<Any>(objects)
        }
        return true
    }

    override fun execute(p0: Event) {
        val popup = PopupManagerImpl.getPopup(popup.getSingle(p0) ?: return) ?: return
        val obj = objects?.getAll(p0) ?: emptyArray()
        val getKey = key?.getSingle(p0)
        player.getAll(p0).forEach { p ->
            val event = CustomPopupEvent(p, "").apply {
                obj.forEachIndexed { i, s ->
                    variables["skript_${i + 1}"] = s.toString()
                }
            }
            runCatching {
                popup.show(
                    BukkitEventUpdateEvent(
                        event,
                        getKey ?: UUID.randomUUID()
                    ), p.toHud() ?: return)
            }
        }

    }
}