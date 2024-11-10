package kr.toxicity.hud.bootstrap.bukkit.compatibility.oraxen

import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.warn
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class OraxenCompatibility : Compatibility {
    override val website: String = "https://www.spigotmc.org/resources/72448/"
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf()

    override fun start() {
        (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
        val version = (Bukkit.getPluginManager().getPlugin("Oraxen") ?: return)
            .description
            .version
        when (version
            .substringBefore('.')
            .toInt()
        ) {
            1 -> {
                (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
                OraxenR1Handler().handle(BOOTSTRAP as Plugin)
            }
            2 -> {
                (BOOTSTRAP as BukkitBootstrapImpl).skipInitialReload = true
                OraxenR2Handler().handle(BOOTSTRAP as Plugin)
            }
            else -> warn("Unknown Oraxen Version.")
        }
        info(
            "BetterHud hooks Oraxen $version.",
            "Be sure to set 'pack-type' to 'none' in your config."
        )
    }
}