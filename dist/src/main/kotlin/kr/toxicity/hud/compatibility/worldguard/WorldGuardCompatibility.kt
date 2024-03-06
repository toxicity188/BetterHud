package kr.toxicity.hud.compatibility.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.compatibility.Compatibility
import org.bukkit.configuration.ConfigurationSection

class WorldGuardCompatibility: Compatibility {
    override val listeners: Map<String, (ConfigurationSection) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "inregion" to HudPlaceholder.of(1) { player, args ->
                val loc = player.bukkitPlayer.location
                WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(loc.world))?.getRegion(args[0])?.contains(loc.blockX, loc.blockY, loc.blockZ) ?: false
            }
        )
}