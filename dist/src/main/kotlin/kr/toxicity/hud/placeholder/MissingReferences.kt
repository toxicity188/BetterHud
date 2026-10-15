package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.warn
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Fork: a placeholder or trigger that only exists on some servers (registered by another plugin)
 * must not make the popup fail to load, otherwise every server generates a different resource pack.
 * Missing references resolve to inert stand-ins and are logged once per reload.
 */
object MissingReferences {
    private val warned = ConcurrentHashMap.newKeySet<String>()

    fun reset() = warned.clear()

    fun warnOnce(kind: String, name: String) {
        if (warned.add("$kind:$name")) warn("[BetterHud] This $kind not found, using an empty stand-in: $name")
    }

    val trigger: HudTrigger<Any> = object : HudTrigger<Any> {
        override fun getKey(t: Any): Any = UUID.randomUUID()
        override fun registerEvent(eventConsumer: java.util.function.BiConsumer<UUID, UpdateEvent>) {}
    }
}

class MissingPlaceholderBuilder(
    private val name: String,
    override val clazz: Class<out Any> = String::class.java,
) : PlaceholderBuilder<Any> {

    private val default: Any = when {
        isNumber -> 0.0
        isBoolean -> false
        else -> ""
    }

    fun typed(target: Class<out Any>) = if (target == clazz) this else MissingPlaceholderBuilder(name, target)

    override fun build(reason: UpdateEvent): Placeholder<Any> = object : Placeholder<Any> {
        override val clazz: Class<out Any> get() = this@MissingPlaceholderBuilder.clazz
        override fun invoke(p1: HudPlayer): Any = default
        override fun stringValue(player: HudPlayer): String = default.toString()
    }

    override fun assertNumber(message: PlaceholderBuilder<Any>.() -> String) = typed(Double::class.javaObjectType)
    override fun assertBoolean(message: PlaceholderBuilder<Any>.() -> String) = typed(Boolean::class.javaObjectType)
    override fun assertString(message: PlaceholderBuilder<Any>.() -> String) = typed(String::class.java)
}
