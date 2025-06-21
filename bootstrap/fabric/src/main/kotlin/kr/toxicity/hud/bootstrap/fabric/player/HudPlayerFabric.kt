package kr.toxicity.hud.bootstrap.fabric.player

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.bootstrap.fabric.FabricBootstrapImpl
import kr.toxicity.hud.bootstrap.fabric.util.hasPermission
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.asyncTaskLater
import net.kyori.adventure.audience.Audience
import net.minecraft.server.MinecraftServer
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*

class HudPlayerFabric(
    private val server: MinecraftServer,
    private val listener: ServerGamePacketListenerImpl
) : HudPlayerImpl() {

    private fun initBossBar(action: () -> Unit) {
        val event = ArrayList<CustomBossEvent>()
        server.customBossEvents.events.forEach {
            if (it.players.any { p ->
                p.uuid == listener.player.uuid
            }) {
                it.removePlayer(listener.player)
                event += it
            }
        }
        action()
        asyncTaskLater(20) {
            event.forEach {
                it.addPlayer(listener.player)
            }
        }
    }

    init {
        initBossBar {
            inject()
        }
    }

    override fun reload() {
        initBossBar {
            super.reload()
        }
    }

    override fun updatePlaceholder() {
    }

    override fun audience(): Audience = listener.player

    override fun locale(): Locale {
        val split = listener.player.clientInformation().language.split('_')
        return if (split.size == 1) Locale.of(split[0].lowercase()) else Locale.of(split[0].lowercase(), split[1].uppercase())
    }

    override fun hasPermission(perm: String): Boolean = listener.player.hasPermission(perm)

    override fun uuid(): UUID = listener.player.uuid
    override fun name(): String = listener.player.scoreboardName

    override fun location(): LocationWrapper = LocationWrapper(
        world(),
        listener.player.x,
        listener.player.y,
        listener.player.z,
        listener.player.xRot,
        listener.player.yRot
    )

    override fun world(): WorldWrapper = (BOOTSTRAP as FabricBootstrapImpl).wrap(listener.player.level())

    override fun handle(): Any = listener.player
}