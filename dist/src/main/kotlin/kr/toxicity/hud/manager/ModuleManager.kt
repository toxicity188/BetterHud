package kr.toxicity.hud.manager

import kr.toxicity.hud.module.MODULE_BUKKIT
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.task
import kr.toxicity.hud.util.warn
import java.util.function.Function

object ModuleManager: BetterHudManager {
    override fun start() {
        MODULE_BUKKIT.forEach { module ->
            runCatching {
                val value = module.value()
                value.triggers.forEach { trigger ->
                    TriggerManagerImpl.addTrigger("${module.key}_${trigger.key}", trigger.value)
                }
                value.listeners.forEach { listener ->
                    ListenerManagerImpl.addListener("${module.key}_${listener.key}") { c ->
                        val original = listener.value(c)
                        Function { f ->
                            original(f)
                        }
                    }
                }
                value.strings.forEach { string ->
                    PlaceholderManagerImpl.stringContainer.addPlaceholder("${module.key}_${string.key}", string.value)
                }
                value.numbers.forEach { number ->
                    PlaceholderManagerImpl.numberContainer.addPlaceholder("${module.key}_${number.key}", number.value)
                }
                value.booleans.forEach { boolean ->
                    PlaceholderManagerImpl.booleanContainer.addPlaceholder("${module.key}_${boolean.key}", boolean.value)
                }
            }.onFailure { e ->
                warn("Unable to load this module: ${module.key}")
                warn("Reason: ${e.message}")
            }
        }
    }

    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}