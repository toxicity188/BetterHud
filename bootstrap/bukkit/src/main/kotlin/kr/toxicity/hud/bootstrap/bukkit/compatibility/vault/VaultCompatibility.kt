package kr.toxicity.hud.bootstrap.bukkit.compatibility.vault

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import java.util.function.Function

class VaultCompatibility : Compatibility {

    private val money = runCatching {
        Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
    }.getOrNull()

    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
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