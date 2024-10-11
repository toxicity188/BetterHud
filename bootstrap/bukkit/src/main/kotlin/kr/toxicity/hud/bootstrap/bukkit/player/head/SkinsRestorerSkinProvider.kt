package kr.toxicity.hud.bootstrap.bukkit.player.head

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.player.head.PlayerSkinProvider
import net.skinsrestorer.api.SkinsRestorerProvider
import org.bukkit.Bukkit
import java.util.*

class SkinsRestorerSkinProvider : PlayerSkinProvider {
    override fun provide(player: HudPlayer): String? {
        return provide(player.uuid())
    }

    override fun provide(playerName: String): String? {
        return Bukkit.getPlayer(playerName)?.let {
            provide(it.uniqueId)
        }
    }

    private fun provide(uuid: UUID): String? {
        return SkinsRestorerProvider.get().playerStorage.getSkinOfPlayer(uuid).map {
            it.value
        }.orElse(null)
    }
}