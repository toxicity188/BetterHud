package kr.toxicity.hud.bootstrap.velocity.util

import com.velocitypowered.api.proxy.Player
import kr.toxicity.hud.api.player.HudPlayer

val HudPlayer.velocityPlayer
    get() = handle() as Player