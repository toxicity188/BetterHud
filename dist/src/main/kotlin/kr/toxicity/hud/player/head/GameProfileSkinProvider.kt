package kr.toxicity.hud.player.head

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.manager.PlayerManagerImpl
import kr.toxicity.hud.util.textures

class GameProfileSkinProvider : PlayerSkinProvider {
    override fun provide(player: HudPlayer): String {
        return player.textures
    }

    override fun provide(playerName: String): String {
        return PlayerManagerImpl.getHudPlayer(playerName)?.textures ?: ""
    }
}