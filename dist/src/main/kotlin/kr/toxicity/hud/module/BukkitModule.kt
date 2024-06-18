package kr.toxicity.hud.module

import kr.toxicity.hud.api.trigger.HudBukkitEventTrigger
import org.bukkit.configuration.ConfigurationSection

interface BukkitModule: Module {
    override val triggers: Map<String, (ConfigurationSection) -> HudBukkitEventTrigger<*>>
}