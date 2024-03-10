package kr.toxicity.hud.module

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import org.bukkit.configuration.ConfigurationSection

interface Module {
    val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>

    val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>

    val numbers: Map<String, HudPlaceholder<Number>>
    val strings: Map<String, HudPlaceholder<String>>
    val booleans: Map<String, HudPlaceholder<Boolean>>

    operator fun plus(other: Module): Module {
        val l2 = listeners + other.listeners
        val t2 = triggers + other.triggers
        val n2 = numbers + other.numbers
        val s2 = strings + other.strings
        val b2 = booleans + other.booleans
        return object : Module {
            override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
                get() = l2
            override val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>
                get() = t2
            override val numbers: Map<String, HudPlaceholder<Number>>
                get() = n2
            override val strings: Map<String, HudPlaceholder<String>>
                get() = s2
            override val booleans: Map<String, HudPlaceholder<Boolean>>
                get() = b2

        }
    }
}