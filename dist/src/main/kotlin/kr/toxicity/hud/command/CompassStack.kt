package kr.toxicity.hud.command

import kr.toxicity.hud.api.compass.Compass
import java.util.Collections

class CompassStack(
    private val compassList: Collection<Compass>
) : Iterable<Compass> {
    override fun iterator(): Iterator<Compass> = Collections.unmodifiableCollection(compassList).iterator()
}