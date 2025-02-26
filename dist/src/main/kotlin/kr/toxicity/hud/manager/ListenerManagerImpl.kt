package kr.toxicity.hud.manager

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.manager.ListenerManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.placeholder.PlaceholderSource
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.JavaNumber
import kr.toxicity.hud.util.ifNull
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function
import kotlin.math.abs

object ListenerManagerImpl : BetterHudManager, ListenerManager {

    private const val MIN_THRESHOLD = 0.01
    private const val DELTA_MIN_THRESHOLD = 0.1
    private const val DELTA_THRESHOLD = 0.75

    private val listenerMap = mutableMapOf<String, (YamlObject) -> (UpdateEvent) -> HudListener>(
        "placeholder" to placeholder@ { c ->
            val source = PlaceholderSource.Impl(c)
            val v = PlaceholderManagerImpl.find(c["value"]?.asString().ifNull { "value value not set." }, source)
            val m = PlaceholderManagerImpl.find(c["max"]?.asString().ifNull { "max value not set." }, source)
            return@placeholder { event ->
                val value = v build event
                val max = m build event
                if (value.clazz == max.clazz && value.clazz == JavaNumber::class.java) {
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

    private fun Double.checkMinThreshold(other: Double) = other <= this + MIN_THRESHOLD && other >= this - MIN_THRESHOLD

    fun getListener(section: YamlObject): (UpdateEvent) -> HudListener {
        val clazz = section["class"]?.asString().ifNull { "class value not set." }
        val listener = listenerMap[clazz].ifNull { "this class doesn't exist: $clazz" }(section)
        if (section.getAsBoolean("lazy", false)) {
            val setting = LazyListenerSetting(section)
            val initialValue: (UpdateEvent) -> (HudPlayer) -> Double = section["initial-value"]?.asString()?.let {
                PlaceholderManagerImpl.find(it, PlaceholderSource.Impl(section)).assertNumber {
                    "this index is not a number. it is ${this.clazz.simpleName}."
                }.let {
                    { reason ->
                        (it build reason).let { placeholder ->
                            { player ->
                                (placeholder(player) as Number).toDouble()
                            }
                        }
                    }
                }
            } ?: {
                {
                    1.0
                }
            }
            return { event: UpdateEvent ->
                LazyListener(
                    event.key,
                    listener(event),
                    setting,
                    initialValue(event)
                )
            }
        } else return listener
    }

    private data class LazyValueKey(
        val uuid: UUID,
        val key: Any
    )

    private class LazyValueAccess(
        private val delay: Int,
        private val multiplier: Double,
        private var value: Double
    ) : (Double) -> Double {
        private var t = 0
        private var delta = 0.0
        override fun invoke(plus: Double): Double {
            val newDelta = plus - value
            if (abs(newDelta) >= DELTA_MIN_THRESHOLD && abs(newDelta - delta) > DELTA_THRESHOLD * abs(delta)) t = 0
            delta = newDelta
            if (++t < delay) return value
            value = if (value.checkMinThreshold(plus)) plus else value * (1 - multiplier) + plus * multiplier
            return value
        }
    }

    private class LazyListenerSetting(section: YamlObject) {
        val delay = section.getAsInt("delay", 0).coerceAtLeast(0)
        val multiplier = section.getAsDouble("multiplier", 0.5).coerceAtLeast(0.0).coerceAtMost(1.0) 
        val cache: ExpiringMap<LazyValueKey, LazyValueAccess> = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(section.getAsLong("expiring-second", 5).coerceAtLeast(1), TimeUnit.SECONDS)
            .build<LazyValueKey, LazyValueAccess>()
    }

    private class LazyListener(
        val key: Any,
        val delegate: HudListener,
        val setting: LazyListenerSetting,
        val initialBuild: (HudPlayer) -> Double,
    ) : HudListener {
        private fun HudPlayer.key() = LazyValueKey(uuid(), key)

        override fun getValue(player: HudPlayer): Double {
            val get = delegate.getValue(player)
            return setting.cache.computeIfAbsent(player.key()) {
                LazyValueAccess(
                    setting.delay,
                    setting.multiplier,
                    initialBuild(player).coerceAtLeast(0.0).coerceAtMost(1.0)
                )
            }(get)
        }

        override fun clear(player: HudPlayer) {
            setting.cache.remove(player.key())
            delegate.clear(player)
        }
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
    override fun reload(info: ReloadInfo, resource: GlobalResource) {
    }

    override fun end() {
    }
}