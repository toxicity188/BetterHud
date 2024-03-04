package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer

interface PlaceholderTask: (HudPlayer) -> Unit {
    val tick: Int
}