package kr.toxicity.hud.manager

import kr.toxicity.hud.api.event.HudPlayerJoinEvent
import kr.toxicity.hud.api.event.HudPlayerQuitEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.pack.PackUploader
import kr.toxicity.hud.player.location.GPSLocationProvider
import kr.toxicity.hud.player.location.PointedLocationProvider
import kr.toxicity.hud.resource.GlobalResource
import kr.toxicity.hud.util.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object PlayerManager: BetterHudManager {

    private val hudPlayer = ConcurrentHashMap<UUID, HudPlayer>()

    private val locationProviders = ArrayList<PointedLocationProvider>()

    override fun start() {
        val manager = Bukkit.getPluginManager()
        if (manager.isPluginEnabled("GPS")) locationProviders.add(GPSLocationProvider())
        manager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            fun join(e: PlayerJoinEvent) {
                register(e.player)
            }
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                hudPlayer.remove(player.uniqueId)?.let {
                    it.cancel()
                    HudPlayerQuitEvent(it).call()
                    asyncTask {
                        it.save()
                    }
                }
            }
        }, PLUGIN)
    }
    fun register(player: Player) {
        if (ConfigManagerImpl.disableToBedrockPlayer && PLUGIN.bedrockAdapter.isBedrockPlayer(player.uniqueId)) return
        val adaptedPlayer = if (PLUGIN.isFolia) PLUGIN.nms.getFoliaAdaptedPlayer(player) else player
        hudPlayer.computeIfAbsent(adaptedPlayer.uniqueId) {
            CompletableFuture.supplyAsync {
                val hud = DatabaseManagerImpl.currentDatabase.load(adaptedPlayer)
                task {
                    taskLater(20) {
                        PackUploader.apply(player)
                    }
                    HudPlayerJoinEvent(hud).call()
                }
                hud
            }.join()
        }
    }

    fun provideLocation(player: HudPlayer) {
        val set = player.pointedLocation
        synchronized(set) {
            set.clear()
            locationProviders.forEach {
                it.provide(player)?.let { loc ->
                    set.add(loc)
                }
            }
        }
    }

    fun getHudPlayer(player: Player) = hudPlayer[player.uniqueId] ?: throw RuntimeException("player is not online!")
    fun getHudPlayer(uuid: UUID) = hudPlayer[uuid]

    override fun preReload() {
        hudPlayer.values.forEach {
            it.popupGroupIteratorMap.forEach { value ->
                value.value.clear()
            }
            it.popupGroupIteratorMap.clear()
            it.popupKeyMap.clear()
        }
    }

    override fun reload(resource: GlobalResource, callback: () -> Unit) {
        hudPlayer.values.forEach {
            it.resetElements()
        }
        callback()
    }

    override fun postReload() {
        hudPlayer.values.forEach {
            it.startTick()
        }
    }

    override fun end() {
        hudPlayer.values.forEach {
            it.save()
        }
    }
}