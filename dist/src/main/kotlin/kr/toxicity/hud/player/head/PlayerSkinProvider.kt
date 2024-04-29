package kr.toxicity.hud.player.head

import org.bukkit.entity.Player

interface PlayerSkinProvider {
    fun provide(player: Player): String?
}