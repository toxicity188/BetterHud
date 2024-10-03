package kr.toxicity.hud.bootstrap.bukkit.compatibility.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import kr.toxicity.hud.api.listener.HudListener
import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.trigger.HudTrigger
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.bootstrap.bukkit.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.bukkit.util.bukkitPlayer
import java.util.function.Function

class WorldGuardCompatibility: Compatibility {
    override val triggers: Map<String, (YamlObject) -> HudTrigger<*>>
        get() = mapOf()
    override val listeners: Map<String, (YamlObject) -> (UpdateEvent) -> HudListener>
        get() = mapOf()
    override val numbers: Map<String, HudPlaceholder<Number>>
        get() = mapOf()
    override val strings: Map<String, HudPlaceholder<String>>
        get() = mapOf()
    override val booleans: Map<String, HudPlaceholder<Boolean>>
        get() = mapOf(
            "in_region" to object : HudPlaceholder<Boolean> {
                override fun getRequiredArgsLength(): Int = 1
                override fun invoke(args: MutableList<String>, reason: UpdateEvent): Function<HudPlayer, Boolean> {
                    return Function { p ->
                        val loc = p.bukkitPlayer.location
                        WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(loc.world))?.getRegion(args[0])?.contains(loc.blockX, loc.blockY, loc.blockZ) ?: false
                    }
                }
            }
        )
}