package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import java.util.UUID

class PopupIteratorImpl(
    private val uuid: UUID,
    private val name: String,
    private val mapper: (Int,  Int) -> List<WidthComponent>,
    val value: () -> Int,
    private val condition: () -> Boolean,
    private val removeTask: () -> Unit,
): PopupIterator {
    private var tick = 0
    private var i = 0

    override fun getUUID(): UUID {
        return uuid
    }
    override fun getIndex(): Int = i
    override fun setIndex(index: Int) {
        i = index
    }
    override fun available() = condition()

    override fun next(): List<WidthComponent> {
        return mapper(i, tick++)
    }

    override fun remove() {
        removeTask()
    }

    override fun getPriority(): Int = value()
    override fun name(): String = name
}