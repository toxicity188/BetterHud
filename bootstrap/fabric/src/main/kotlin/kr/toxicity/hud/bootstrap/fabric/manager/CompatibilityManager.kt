package kr.toxicity.hud.bootstrap.fabric.manager

import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bootstrap.fabric.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.fabric.compatibility.TextPlaceholderAPICompatibility
import kr.toxicity.hud.util.CONSOLE
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.runWithExceptionHandling
import net.fabricmc.loader.api.FabricLoader
import java.util.function.Function

object CompatibilityManager {

    private val compatibilities: Map<String, () -> Compatibility> = mapOf(
        "placeholder-api" to {
            TextPlaceholderAPICompatibility()
        }
    )

    fun start() {
        compatibilities.forEach {
            if (FabricLoader.getInstance().isModLoaded(it.key)) {
                runWithExceptionHandling(CONSOLE, "Unable to load ${it.key} support.") {
                    val obj = it.value()
                    val namespace = it.key.lowercase().replace('-', '_')
                    obj.listeners.forEach { entry ->
                        PLUGIN.listenerManager.addListener("${namespace}_${entry.key}") { c ->
                            val reason = entry.value(c)
                            Function { u: UpdateEvent ->
                                reason(u)
                            }
                        }
                    }
                    obj.numbers.forEach { entry ->
                        PLUGIN.placeholderManager.numberContainer.addPlaceholder("${namespace}_${entry.key}", entry.value)
                    }
                    obj.strings.forEach { entry ->
                        PLUGIN.placeholderManager.stringContainer.addPlaceholder("${namespace}_${entry.key}", entry.value)
                    }
                    obj.booleans.forEach { entry ->
                        PLUGIN.placeholderManager.booleanContainer.addPlaceholder("${namespace}_${entry.key}", entry.value)
                    }
                    obj.triggers.forEach { entry ->
                        PLUGIN.triggerManager.addTrigger("${namespace}_${entry.key}", entry.value)
                    }
                }
            }
        }
    }
}