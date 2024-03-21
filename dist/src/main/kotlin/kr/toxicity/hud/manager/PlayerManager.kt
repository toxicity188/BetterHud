package kr.toxicity.hud.manager

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.PLUGIN
import kr.toxicity.hud.util.asyncTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerManager: BetterHudManager {

    private val hudPlayer = ConcurrentHashMap<UUID, HudPlayer>()

    override fun start() {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun join(e: PlayerJoinEvent) {
                if (ConfigManager.disableToBedrockPlayer && PLUGIN.bedrockAdapter.isBedrockPlayer(e.player.uniqueId)) return
                val player = if (PLUGIN.isFolia) PLUGIN.nms.getFoliaAdaptedPlayer(e.player) else e.player
                asyncTask {
                    hudPlayer.computeIfAbsent(player.uniqueId) {
                        DatabaseManagerImpl.currentDatabase.load(player)
                    }
                }
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                hudPlayer.remove(e.player.uniqueId)?.let {
                    it.cancel()
                    asyncTask {
                        it.save()
                    }
                }
            }
        }, PLUGIN)
    }

    fun getHudPlayer(player: Player) = hudPlayer[player.uniqueId] ?: throw RuntimeException("player is not online!")
    fun getHudPlayer(uuid: UUID) = hudPlayer[uuid]
    override fun reload(resource: GlobalResource) {
        hudPlayer.values.forEach {
            it.popupGroupIteratorMap.forEach { value ->
                value.value.clear()
            }
            it.popupGroupIteratorMap.clear()
            it.popupKeyMap.clear()
            it.resetElements()
            it.startTick()
        }
    }

    override fun end() {
        hudPlayer.values.forEach {
            it.save()
        }
    }
}