package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.util.forEachSubConfiguration
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.warn
import org.bukkit.configuration.ConfigurationSection

object Conditions {


    fun parse(section: ConfigurationSection): (HudPlayer) -> Boolean {
        var value: (HudPlayer) -> Boolean = { true }
        section.forEachSubConfiguration { s, configurationSection ->
            runCatching {
                val new = parse0(configurationSection)
                val old = value
                value = when (val gate = configurationSection.getString("gate") ?: "and") {
                    "and" -> {
                        {
                            old(it) && new(it)
                        }
                    }

                    "or" -> {
                        {
                            old(it) || new(it)
                        }
                    }
                    else -> {
                        throw RuntimeException("this gate doesn't exist: $gate")
                    }
                }
            }.onFailure { e ->
                e.printStackTrace()
                warn("Unable to load this condition: $s")
                warn("Reason: ${e.message}")
            }
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    private fun parse0(section: ConfigurationSection): (HudPlayer) -> Boolean {
        val first = Placeholders.find(section.getString("first").ifNull("first value not set."))
        val second = Placeholders.find(section.getString("second").ifNull("second value not set."))
        val operationValue = section.getString("operation").ifNull("operation value not set")

        if (first.clazz != second.clazz) throw RuntimeException("type mismatch: ${first.clazz.simpleName} and ${second.clazz.simpleName}")

        val operation = (Operations.find(first.clazz) ?: throw RuntimeException("unable to load valid operation. you need to call developer.")).map[section.getString("operation").ifNull(operationValue)].ifNull("unsupported operation: $operationValue") as (Any, Any) -> Boolean
        return { p ->
            operation(first.value(p), second.value(p))
        }
    }
}