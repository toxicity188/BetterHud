package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PlayerManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocationProvider
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.resource.GlobalResource
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerManagerImpl : BetterHudManager, PlayerManager {

    override val managerName: String = "Player"
    override val supportExternalPacks: Boolean = false

    private val playerMap = ConcurrentHashMap<UUID, HudPlayer>()
    private val stringPlayer = ConcurrentHashMap<String, HudPlayer>()

    private val locationProviders = mutableListOf(
        PointedLocationProvider { player -> player.pointers() }
    )

    override fun start() {
    }

    fun provideLocation(player: HudPlayer) {
        val set = player.pointedLocation
        synchronized(set) {
            set.clear()
            locationProviders.forEach {
                val provided = it.provide(player)
                if (provided.isNotEmpty()) set += provided
            }
        }
    }

    fun addHudPlayer(uuid: UUID, player: () -> HudPlayerImpl): HudPlayer {
        return playerMap.computeIfAbsent(uuid) {
            player().apply {
                stringPlayer[name()] = this
            }
        }
    }
    fun removeHudPlayer(uuid: UUID) = playerMap.remove(uuid)?.apply {
        stringPlayer.remove(name())
    }

    override fun getAllHudPlayer(): Collection<HudPlayer> = Collections.unmodifiableCollection(playerMap.values)

    override fun getHudPlayer(uuid: UUID) = playerMap[uuid]

    fun getHudPlayer(name: String) = stringPlayer[name]

    override fun addLocationProvider(provider: PointedLocationProvider) {
        locationProviders += provider
    }

    override fun preReload() {
        playerMap.values.forEach {
            it.popupGroupIteratorMap.forEach { value ->
                value.value.clear()
            }
            it.popupGroupIteratorMap.clear()
            it.popupKeyMap.clear()
        }
    }

    override fun reload(workingDirectory: File, info: ReloadInfo, resource: GlobalResource) {
    }

    override fun postReload() {
        playerMap.values.forEach(HudPlayer::reload)
    }

    override fun end() {
        val list = ArrayList(playerMap.values)
        playerMap.clear()
        list.forEach {
            it.save()
        }
    }
}