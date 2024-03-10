package kr.toxicity.hud.manager

import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.compatibility.mmocore.MMOCoreCompatibility
import kr.toxicity.hud.compatibility.mythicmobs.MythicMobsCompatibility
import kr.toxicity.hud.compatibility.worldguard.WorldGuardCompatibility
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.warn
import org.bukkit.Bukkit
import java.util.function.Function

object CompatibilityManager: BetterHudManager {

    private val compatibilities = mapOf(
        "MMOCore" to {
            MMOCoreCompatibility()
        },
        "MythicMobs" to {
            MythicMobsCompatibility()
        },
        "WorldGuard" to {
            WorldGuardCompatibility()
        }
    )

    override fun start() {
        compatibilities.forEach {
            if (Bukkit.getPluginManager().isPluginEnabled(it.key)) {
                runCatching {
                    val obj = it.value()
                    val namespace = it.key.lowercase()
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
                }.onFailure { e ->
                    warn("Unable to load ${it.key} support.")
                    warn("Reason: ${e.message}")
                }
            }
        }
    }

    override fun reload(resource: GlobalResource) {
    }

    override fun end() {
    }
}