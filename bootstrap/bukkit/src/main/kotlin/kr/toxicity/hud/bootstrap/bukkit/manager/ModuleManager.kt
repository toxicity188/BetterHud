package kr.toxicity.hud.bootstrap.bukkit.manager

import kr.toxicity.hud.bootstrap.bukkit.module.MODULE_BUKKIT
import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.TriggerManagerImpl
import kr.toxicity.hud.util.handleFailure
import java.util.function.Function

object ModuleManager {
    fun start() {
        MODULE_BUKKIT.forEach { module ->
            runCatching {
                val value = module.value()
                value.start()
                fun String.key(tag: String) = if (this == "standard") tag else "${this}_$tag"
                value.triggers.forEach { trigger ->
                    TriggerManagerImpl.addTrigger(module.key.key(trigger.key), trigger.value)
                }
                value.listeners.forEach { listener ->
                    ListenerManagerImpl.addListener(module.key.key(listener.key)) { c ->
                        val original = listener.value(c)
                        Function { f ->
                            original(f)
                        }
                    }
                }
                value.strings.forEach { string ->
                    PlaceholderManagerImpl.stringContainer.addPlaceholder(module.key.key(string.key), string.value)
                }
                value.numbers.forEach { number ->
                    PlaceholderManagerImpl.numberContainer.addPlaceholder(module.key.key(number.key), number.value)
                }
                value.booleans.forEach { boolean ->
                    PlaceholderManagerImpl.booleanContainer.addPlaceholder(module.key.key(boolean.key), boolean.value)
                }
            }.handleFailure {
                "Unable to load this module: ${module.key}"
            }
        }
    }

}