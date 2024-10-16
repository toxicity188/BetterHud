package kr.toxicity.hud.manager

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.manager.ListenerManager
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.ifNull
import net.kyori.adventure.audience.Audience
import java.util.function.Function

object ListenerManagerImpl : BetterHudManager, ListenerManager {

    private val listenerMap = mutableMapOf<String, (YamlObject) -> (UpdateEvent) -> HudListener>(
        "placeholder" to placeholder@ { c ->
            val v = PlaceholderManagerImpl.find(c.get("value")?.asString().ifNull("value value not set."))
            val m = PlaceholderManagerImpl.find(c.get("max")?.asString().ifNull("max value not set."))
            return@placeholder { event ->
                val value = v.build(event)
                val max = m.build(event)
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

    fun getListener(section: YamlObject): (UpdateEvent) -> HudListener {
        val clazz = section.get("class")?.asString().ifNull("class value not set.")
        return listenerMap[clazz].ifNull("this class doesn't exist: $clazz")(section)
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