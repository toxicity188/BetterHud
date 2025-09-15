package kr.toxicity.hud.bootstrap.bukkit.manager

import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.bootstrap.bukkit.compatibility.craftengine.CraftEngineCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mmocore.MMOCoreCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mmoitems.MMOItemsCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mythiclib.MythicLibCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.mythicmobs.MythicMobsCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.nexo.NexoCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.parties.PartiesCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.skript.SkriptCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.vault.VaultCompatibility
import kr.toxicity.hud.bootstrap.bukkit.compatibility.worldguard.WorldGuardCompatibility
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.handleFailure
import org.bukkit.Bukkit
import java.util.function.Function

object CompatibilityManager {

    val compatibilities = mapOf(
        "MMOCore" to {
            MMOCoreCompatibility()
        },
        "MythicMobs" to {
            MythicMobsCompatibility()
        },
        "WorldGuard" to {
            WorldGuardCompatibility()
        },
        "Vault" to {
            VaultCompatibility()
        },
        "MythicLib" to {
            MythicLibCompatibility()
        },
        "Skript" to {
            SkriptCompatibility()
        },
        "MMOItems" to {
            MMOItemsCompatibility()
        },
        "Parties" to {
            PartiesCompatibility()
        },
        "CraftEngine" to {
            CraftEngineCompatibility()
        },
        "Nexo" to {
            NexoCompatibility()
        }
    )

    fun start() {
        compatibilities.forEach {
            if (Bukkit.getPluginManager().isPluginEnabled(it.key)) {
                val obj = it.value()
                runCatching {
                    obj.start()
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
                    obj.triggers.forEach { entry ->
                        PLUGIN.triggerManager.addTrigger("${namespace}_${entry.key}", entry.value)
                    }
                }.handleFailure {
                    "Unable to load ${it.key} support. checks this: ${obj.website}"
                }
            }
        }
    }
}