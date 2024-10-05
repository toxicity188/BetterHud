package kr.toxicity.hud.bootstrap.fabric.compatibility

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject

interface Compatibility {
    val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
    val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
    val numbers: Map<String, HudPlaceholder<Number>>
    val strings: Map<String, HudPlaceholder<String>>
    val booleans: Map<String, HudPlaceholder<Boolean>>
}