package kr.toxicity.hud.bootstrap.fabric.player

import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.bootstrap.fabric.FabricBootstrapImpl
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import net.kyori.adventure.audience.Audience
import net.minecraft.server.level.ServerPlayer
import java.util.*

class HudPlayerFabric(
    private val player: ServerPlayer,
    private val audience: Audience
): HudPlayerImpl() {

    init {
        inject()
    }

    override fun updatePlaceholder() {
    }

    override fun audience(): Audience = audience

    override fun type(): CommandSourceWrapper.Type {
        return CommandSourceWrapper.Type.PLAYER
    }

    override fun hasPermission(perm: String): Boolean = isOp

    override fun isOp(): Boolean = player.hasPermissions(2)

    override fun uuid(): UUID = player.uuid
    override fun name(): String = player.scoreboardName

    override fun location(): LocationWrapper = LocationWrapper(
        world(),
        player.x,
        player.y,
        player.z,
        player.xRot,
        player.yRot
    )

    override fun world(): WorldWrapper = (BOOTSTRAP as FabricBootstrapImpl).wrap(player.serverLevel())

    override fun handle(): Any = player
}