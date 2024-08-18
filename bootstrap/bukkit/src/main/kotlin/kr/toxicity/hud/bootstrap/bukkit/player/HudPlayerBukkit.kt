package kr.toxicity.hud.bootstrap.bukkit.player

import kr.toxicity.hud.api.adapter.CommandSourceWrapper
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player
import java.util.*

class HudPlayerBukkit(
    private val player: Player,
    private val audience: Audience
): HudPlayerImpl() {
    override fun uuid(): UUID = player.uniqueId
    override fun name(): String = player.name
    override fun handle(): Any = player
    override fun audience(): Audience = audience
    override fun world(): WorldWrapper = WorldWrapper(
        player.world.name,
        player.world.uid
    )

    override fun location(): LocationWrapper {
        val loc = player.location
        return LocationWrapper(
            world(),
            loc.x,
            loc.y,
            loc.z,
            loc.pitch,
            loc.yaw
        )
    }

    override fun updatePlaceholder() {
        (BOOTSTRAP as BukkitBootstrapImpl).update(this)
    }

    init {
        inject()
    }

    override fun hasPermission(perm: String): Boolean = player.hasPermission(perm)
    override fun type(): CommandSourceWrapper.Type = CommandSourceWrapper.Type.PLAYER
    override fun isOp(): Boolean = player.isOp
}