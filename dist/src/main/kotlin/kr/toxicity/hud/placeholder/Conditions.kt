package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.warn
import org.bukkit.configuration.ConfigurationSection

object Conditions {
    fun parse(section: ConfigurationSection): ConditionBuilder {
        var value: ConditionBuilder = ConditionBuilder.alwaysTrue
        section.forEachSubConfiguration { s, configurationSection ->
            runCatching {
                val new = parse0(configurationSection)
                value = when (val gate = configurationSection.getString("gate") ?: "and") {
                    "and" -> value.and(new)
                    "or" -> value.or(new)
                    else -> {
                        throw RuntimeException("this gate doesn't exist: $gate")
                    }
                }
            }.onFailure { e ->
                warn("Unable to load this condition: $s")
                warn("Reason: ${e.message}")
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    private fun parse0(section: ConfigurationSection): ConditionBuilder {
        val first = PlaceholderManagerImpl.find(section.getString("first").ifNull("first value not set."))
        val second = PlaceholderManagerImpl.find(section.getString("second").ifNull("second value not set."))
        val operationValue = section.getString("operation").ifNull("operation value not set")

        if (first.clazz != second.clazz) throw RuntimeException("type mismatch: ${first.clazz.simpleName} and ${second.clazz.simpleName}")

        val operation = (Operations.find(first.clazz) ?: throw RuntimeException("unable to load valid operation. you need to call developer.")).map[section.getString("operation").ifNull(operationValue)].ifNull("unsupported operation: $operationValue") as (Any, Any) -> Boolean
        return object : ConditionBuilder {
            override fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean {
                val o1 = first.build(updateEvent)
                val o2 = second.build(updateEvent)
                return { p ->
                    operation(o1.value(p), o2.value(p))
                }
            }
        }
    }
}