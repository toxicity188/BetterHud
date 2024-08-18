package kr.toxicity.hud.bootstrap.velocity.player

import com.velocitypowered.api.proxy.Player
import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.util.asyncTask
import net.kyori.adventure.audience.Audience
import java.util.*

class HudPlayerVelocity(
    private val player: Player,
): HudPlayerImpl() {
    override fun uuid(): UUID = player.uniqueId
    override fun name(): String = player.username
    override fun handle(): Any = player
    override fun audience(): Audience = player

    override fun world(): WorldWrapper = throw UnsupportedOperationException("velocity")
    override fun location(): LocationWrapper = throw UnsupportedOperationException("velocity")
    override fun updatePlaceholder() {
        if (!player.isActive) {
            PlayerManagerImpl.removeHudPlayer(player.uniqueId)?.let {
                it.cancel()
                asyncTask {
                    it.save()
                }
            }
        }
    }

    init {
        inject()
    }

    override fun hasPermission(perm: String): Boolean = player.hasPermission(perm)
    override fun type(): CommandSourceWrapper.Type = CommandSourceWrapper.Type.PLAYER
    override fun isOp(): Boolean = player.hasPermission("betterhud.admin")
}