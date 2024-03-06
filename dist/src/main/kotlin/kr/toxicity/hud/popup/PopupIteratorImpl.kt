package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator

class PopupIteratorImpl(
    private val mapper: (Int) -> List<WidthComponent>,
    private val length: Int,
    private val duration: Int,
    private val condition: () -> Boolean
): PopupIterator {
    private var tick = 0
    override fun available() = (duration < 0 || duration < tick) && condition()

    override fun next(): List<WidthComponent> {
        if (tick >= length) tick = 0
        val value = mapper(tick)
        tick++
        return value
    }
}