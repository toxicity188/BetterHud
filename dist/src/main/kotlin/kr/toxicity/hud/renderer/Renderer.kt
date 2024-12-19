package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.PixelComponent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.util.TickProvider

fun interface Renderer {
    fun render(event: UpdateEvent): TickProvider<HudPlayer, PixelComponent>
}