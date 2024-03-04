package kr.toxicity.hud.manager

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.manager.ListenerManager
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.armor
import kr.toxicity.hud.util.ifNull
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

object ListenerManagerImpl: MythicHudManager, ListenerManager {

    private val listenerMap = mutableMapOf<String, (ConfigurationSection) -> HudListener>(
        "health" to { _ ->
            HudListener { p ->
                p.bukkitPlayer.health / p.bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
            }
        },
        "food" to { _ ->
            HudListener { p ->
                p.bukkitPlayer.foodLevel / 20.0
            }
        },
        "armor" to { _ ->
            HudListener { p ->
                p.bukkitPlayer.armor / 20.0
            }
        },
        "air" to { _ ->
            HudListener { p ->
                p.bukkitPlayer.remainingAir.toDouble() / p.bukkitPlayer.maximumAir
            }
        },
        "exp" to { _ ->
            HudListener { p ->
                p.bukkitPlayer.exp.toDouble()
            }
        },
        "placeholder" to { c ->
            val value = PlaceholderManagerImpl.find(c.getString("value").ifNull("value not set."))
            val max = PlaceholderManagerImpl.find(c.getString("max").ifNull("max not set."))
            if (value.clazz == max.clazz && value.clazz == java.lang.Number::class.java) {
                HudListener {
                    runCatching {
                        (value(it) as Number).toDouble() / (max(it) as Number).toDouble()
                    }.getOrNull() ?: 0.0
                }
            } else throw RuntimeException("this type is not a number: ${value.clazz.simpleName} and ${max.clazz.simpleName}")
        }
    )

    override fun start() {

    }

    fun getListener(section: ConfigurationSection): HudListener {
        val clazz = section.getString("class").ifNull("class value not set.")
        return listenerMap[clazz].ifNull("this class doesn't exist: $clazz")(section)
    }

    override fun addListener(name: String, listenerFunction: Function<ConfigurationSection, HudListener>) {
        listenerMap[name] = { c ->
            listenerFunction.apply(c)
        }
    }
    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}