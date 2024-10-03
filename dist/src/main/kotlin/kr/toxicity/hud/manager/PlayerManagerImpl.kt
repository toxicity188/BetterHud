package kr.toxicity.hud.manager

import kr.toxicity.hud.api.manager.PlayerManager
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.player.PointedLocation
import kr.toxicity.hud.api.player.PointedLocationProvider
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.resource.GlobalResource
import net.kyori.adventure.audience.Audience
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerManagerImpl: BetterHudManager, PlayerManager {

    private val hudPlayer = ConcurrentHashMap<UUID, HudPlayer>()
    private val stringPlayer = ConcurrentHashMap<String, HudPlayer>()

    private val locationProviders = mutableListOf<PointedLocationProvider>(
        object : PointedLocationProvider {
            override fun provide(player: HudPlayer): Collection<PointedLocation> {
                return player.pointers()
            }
        }
    )

    override fun start() {
    }

    fun provideLocation(player: HudPlayer) {
        val set = player.pointedLocation
        synchronized(set) {
            set.clear()
            locationProviders.forEach {
                val provided = it.provide(player)
                if (provided.isNotEmpty()) set.addAll(provided)
            }
        }
    }

    fun addHudPlayer(uuid: UUID, player: () -> HudPlayerImpl) {
        hudPlayer.computeIfAbsent(uuid) {
            player().apply {
                stringPlayer[name()] = this
            }
        }
    }
    fun removeHudPlayer(uuid: UUID) = hudPlayer.remove(uuid)?.apply {
        stringPlayer.remove(name())
    }

    override fun getAllHudPlayer(): Collection<HudPlayer> = Collections.unmodifiableCollection(hudPlayer.values)

    override fun getHudPlayer(uuid: UUID) = hudPlayer[uuid]

    fun getHudPlayer(name: String) = stringPlayer[name]

    override fun addLocationProvider(provider: PointedLocationProvider) {
        locationProviders.add(provider)
    }

    override fun preReload() {
        hudPlayer.values.forEach {
            it.popupGroupIteratorMap.forEach { value ->
                value.value.clear()
            }
            it.popupGroupIteratorMap.clear()
            it.popupKeyMap.clear()
        }
    }

    override fun reload(sender: Audience, resource: GlobalResource) {
        hudPlayer.values.forEach {
            it.resetElements()
        }
    }

    override fun postReload() {
        hudPlayer.values.forEach {
            it.startTick()
        }
    }

    override fun end() {
        val list = ArrayList(hudPlayer.values)
        hudPlayer.clear()
        list.forEach {
            it.save()
        }
    }
}