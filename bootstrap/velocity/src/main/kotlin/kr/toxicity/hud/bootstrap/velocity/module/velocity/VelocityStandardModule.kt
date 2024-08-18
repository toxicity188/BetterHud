package kr.toxicity.hud.bootstrap.velocity.module.velocity

import kr.toxicity.hud.api.bukkit.trigger.HudBukkitEventTrigger
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.velocity.module.Module
import kr.toxicity.hud.bootstrap.velocity.util.velocityPlayer
import java.util.function.Function

class VelocityStandardModule: Module {
    override val triggers: Map<String, (YamlObject) -> HudBukkitEventTrigger<*>>
        get() = mapOf(
        )
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf(
        )
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "ping" to HudPlaceholder.of { _, _ ->
                Function {
                    it.velocityPlayer.ping
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf(
            "name" to HudPlaceholder.of { _, _ ->
                Function {
                    it.velocityPlayer.username
                }
            }
        )
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
        )
}