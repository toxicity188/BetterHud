package kr.toxicity.hud.command

import kr.toxicity.hud.api.hud.Hud
import java.util.Collections

class HudStack(
    private val hudList: Collection<Hud>
) : Iterable<Hud> {
    override fun iterator(): Iterator<Hud> = Collections.unmodifiableCollection(hudList).iterator()
}