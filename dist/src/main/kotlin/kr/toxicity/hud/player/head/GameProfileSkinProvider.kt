package kr.toxicity.hud.player.head

import kr.toxicity.hud.util.textures
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GameProfileSkinProvider: PlayerSkinProvider {
    override fun provide(player: Player): String {
        return player.textures
    }

    override fun provide(playerName: String): String {
        return Bukkit.getPlayerExact(playerName)?.textures ?: ""
    }
}