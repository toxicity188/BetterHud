package kr.toxicity.hud.player.head

import kr.toxicity.hud.util.textures
import org.bukkit.entity.Player

class GameProfileSkinProvider: PlayerSkinProvider {
    override fun provide(player: Player): String {
        return player.textures
    }
}