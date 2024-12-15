package kr.toxicity.hud.manager

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.manager.ListenerManager
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.ifNull
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import net.kyori.adventure.audience.Audience
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function

object ListenerManagerImpl : BetterHudManager, ListenerManager {

    private const val THRESHOLD = 0.01

    private val listenerMap = mutableMapOf<String, (YamlObject) -> (UpdateEvent) -> HudListener>(
        "placeholder" to placeholder@ { c ->
            val source = PlaceholderSource.Impl(c)
            val v = PlaceholderManagerImpl.find(c["value"]?.asString().ifNull("value value not set."), source)
            val m = PlaceholderManagerImpl.find(c["max"]?.asString().ifNull("max value not set."), source)
            return@placeholder { event ->
                val value = v build event
                val max = m build event
                if (value.clazz == max.clazz && value.clazz == java.lang.Number::class.java) {
                    HudListener {
                        runCatching {
                            (value(it) as Number).toDouble() / (max(it) as Number).toDouble()
                        }.getOrNull() ?: 0.0
                    }
                } else throw RuntimeException("this type is not a number: ${value.clazz.simpleName} and ${max.clazz.simpleName}")
            }
        }
    )

    override fun start() {

    }

    override fun getAllListenerKeys(): Set<String> = Collections.unmodifiableSet(listenerMap.keys)

    private data class LazyValueKey(
        val uuid: UUID,
        val key: Any
    )

    private class LazyValueAccess(
        private val delay: Int,
        private val multiplier: Double,
        private var value: Double
    ) {
        private var t = 0
        fun apply(plus: Double): Double {
            if (value.checkThreshold(plus)) {
                t = 0
                value = plus
                return value
            }
            if (++t < delay) {
                return value
            }
            value = value * (1 - multiplier) + plus * multiplier
            return value
        }
    }

    private fun Double.checkThreshold(other: Double) = other <= this + THRESHOLD && other >= this - THRESHOLD

    fun getListener(section: YamlObject): (UpdateEvent) -> HudListener {
        val clazz = section["class"]?.asString().ifNull("class value not set.")
        val listener = listenerMap[clazz].ifNull("this class doesn't exist: $clazz")(section)
        if (section.getAsBoolean("lazy", false)) {
            val second = section.getAsLong("expiring-second", 10).coerceAtLeast(1)
            val lazyMap = ExpiringMap.builder()
                .expirationPolicy(ExpirationPolicy.ACCESSED)
                .expiration(second, TimeUnit.SECONDS)
                .build<LazyValueKey, LazyValueAccess>()
            val delay = section.getAsInt("delay", 0).coerceAtLeast(0)
            val multiplier = section.getAsDouble("multiplier", 0.5).coerceAtLeast(0.0).coerceAtMost(1.0)
            val initialValue = section.getAsDouble("initial-value", 1.0).coerceAtLeast(0.0).coerceAtMost(1.0)
            return { event: UpdateEvent ->
                val gen = listener(event)
                HudListener { p ->
                    val get = gen.getValue(p)
                    val other = lazyMap.computeIfAbsent(LazyValueKey(p.uuid(), event.key)) {
                        LazyValueAccess(
                            delay,
                            multiplier,
                            initialValue
                        )
                    }.apply(get)
                    other
                }
            }
        } else return listener
    }

    override fun addListener(name: String, listenerFunction: Function<YamlObject, Function<UpdateEvent, HudListener>>) {
        listenerMap[name] = { c ->
            listenerFunction.apply(c).let { t ->
                {
                    t.apply(it)
                }
            }
        }
    }
    override fun reload(sender: Audience, resource: GlobalResource) {
    }

    override fun end() {
    }
}