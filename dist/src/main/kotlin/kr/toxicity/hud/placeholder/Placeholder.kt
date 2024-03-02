package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer

interface Placeholder<T : Any>: (HudPlayer) -> T {
    val clazz: Class<out T>
    fun value(player: HudPlayer): T = clazz.cast(invoke(player))
}