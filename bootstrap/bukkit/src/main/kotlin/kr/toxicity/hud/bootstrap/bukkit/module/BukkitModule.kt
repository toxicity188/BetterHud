package kr.toxicity.hud.bootstrap.bukkit.module

import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.yaml.YamlObject

interface BukkitModule: Module {
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
}