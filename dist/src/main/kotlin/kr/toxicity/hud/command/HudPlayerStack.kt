package kr.toxicity.hud.command

import kr.toxicity.hud.api.player.HudPlayer
import java.util.Collections

class HudPlayerStack(
    private val playerList: Collection<HudPlayer>
) : Iterable<HudPlayer> {
    override fun iterator(): Iterator<HudPlayer> = Collections.unmodifiableCollection(playerList).iterator()
}