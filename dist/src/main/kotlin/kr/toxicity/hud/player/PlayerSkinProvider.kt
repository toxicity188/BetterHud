package kr.toxicity.hud.player

import org.bukkit.entity.Player

interface PlayerSkinProvider {
    fun provide(player: Player): String?
}