package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupSortType
import java.util.UUID

class PopupIteratorImpl(
    private val maxIndex: Int,
    private val key: Any,
    private val sortType: PopupSortType,
    private val name: String,
    private val mapper: (Int,  Int) -> List<WidthComponent>,
    val value: () -> Int,
    private val condition: () -> Boolean,
    private val removeTask: () -> Unit,
): PopupIterator {
    private var tick = 0
    private var i = 0
    override fun getMaxIndex(): Int = maxIndex

    override fun getSortType(): PopupSortType {
        return sortType
    }
    override fun getIndex(): Int = i
    override fun setIndex(index: Int) {
        i = index
    }
    override fun available() = condition()
    override fun getKey(): Any = key

    override fun next(): List<WidthComponent> {
        return mapper(i, tick++)
    }

    override fun remove() {
        removeTask()
    }

    override fun getPriority(): Int = value()
    override fun name(): String = name
}