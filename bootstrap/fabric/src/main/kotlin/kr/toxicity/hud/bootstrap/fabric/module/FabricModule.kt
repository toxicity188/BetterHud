package kr.toxicity.hud.bootstrap.fabric.module

import kr.toxicity.hud.api.mod.trigger.HudModEventTrigger
import kr.toxicity.hud.api.yaml.YamlObject

interface FabricModule : Module {
    override val triggers: Map<String, (YamlObject) -> HudModEventTrigger<*>>
}