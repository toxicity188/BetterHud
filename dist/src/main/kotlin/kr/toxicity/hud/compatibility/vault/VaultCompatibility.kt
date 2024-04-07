package kr.toxicity.hud.compatibility.vault

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trgger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.compatibility.Compatibility
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.util.function.Function

class VaultCompatibility: Compatibility {

    private val money = runCatching {
        Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
    }.getOrNull()

    override val triggers: Map<String, (ConfigurationSection) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (ConfigurationSection) -> (UpdateEvent) -> HudListener>
        get() = emptyMap()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf(
            "money" to HudPlaceholder.of { _, _ ->
                Function { p ->
                    money?.getBalance(p.bukkitPlayer) ?: 0.0
                }
            }
        )
    override val strings: Map<String, HudPlaceholder<String>>
        get() = emptyMap()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = emptyMap()
}