package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator

class PopupIteratorImpl(
    private val name: String,
    private val mapper: (Int,  Int) -> List<WidthComponent>,
    private val duration: Int,
    private val condition: () -> Boolean
): PopupIterator {
    private var tick = 0
    private var i = 0
    override fun getIndex(): Int = i
    override fun setIndex(index: Int) {
        i = index
    }
    override fun available() = (duration < 0 || duration < tick) && condition()

    override fun next(): List<WidthComponent> {
        return mapper(i, tick++)
    }

    override fun name(): String = name
}