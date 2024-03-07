package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object PlayerManager: MythicHudManager {

    private val hudPlayer = HashMap<UUID, HudPlayer>()

    override fun start() {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun join(e: PlayerJoinEvent) {
                val player = e.player
                hudPlayer[player.uniqueId] = HudPlayerImpl(player)
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                hudPlayer.remove(e.player.uniqueId)?.cancel()
            }
        }, PLUGIN)
    }

    fun getHudPlayer(player: Player) = hudPlayer[player.uniqueId] ?: throw RuntimeException("player is not online!")

    override fun reload(resource: GlobalResource) {
        hudPlayer.values.forEach {
            it.popupGroupIteratorMap.forEach { value ->
                value.value.clear()
            }
            it.popupGroupIteratorMap.clear()
        }
    }

    override fun end() {
    }
}