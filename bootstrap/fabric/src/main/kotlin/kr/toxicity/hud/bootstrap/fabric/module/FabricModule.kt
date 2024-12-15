package kr.toxicity.hud.bootstrap.fabric.module

import kr.toxicity.hud.api.fabric.trigger.HudFabricEventTrigger
import kr.toxicity.hud.api.yaml.YamlObject

interface FabricModule : Module {
    override val triggers: Map<String, (YamlObject) -> HudFabricEventTrigger<*>>
}