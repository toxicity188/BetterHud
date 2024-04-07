package kr.toxicity.hud.compatibility

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import org.bukkit.configuration.ConfigurationSection

interface Compatibility {
    val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>
    val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
    val numbers: Map<String, HudPlaceholder<Number>>
    val strings: Map<String, HudPlaceholder<String>>
    val booleans: Map<String, HudPlaceholder<Boolean>>
}