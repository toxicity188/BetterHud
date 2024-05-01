package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupSortType
import java.util.*

class PopupIteratorImpl(
    private val unique: Boolean,
    private val maxIndex: Int,
    private val key: Any,
    private val sortType: PopupSortType,
    private val name: String,
    private val save: Boolean,
    private val alwaysCheckCondition: Boolean,
    private val mapper: (Int,  Int) -> List<WidthComponent>,
    private var value: Int,
    private val condition: () -> Boolean,
    private val removeTask: () -> Unit,
): PopupIterator {
    private var tick = 0
    private var i = -1
    private var removal = false
    private val id = UUID.randomUUID()
    override fun getMaxIndex(): Int = maxIndex
    override fun getUUID(): UUID = id

    override fun getSortType(): PopupSortType {
        return sortType
    }

    override fun isUnique(): Boolean = unique

    override fun canSave(): Boolean = save
    override fun alwaysCheckCondition(): Boolean = alwaysCheckCondition
    override fun getIndex(): Int = i
    override fun setIndex(index: Int) {
        i = index
    }
    override fun available() = condition()
    override fun getKey(): Any = key

    override fun next(): List<WidthComponent> {
        return mapper(i, tick++)
    }

    override fun markedAsRemoval(): Boolean = removal
    override fun remove() {
        removal = true
        removeTask()
    }

    override fun getPriority(): Int = value
    override fun setPriority(priority: Int) {
        value = priority
    }
    override fun name(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PopupIteratorImpl

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun compareTo(other: PopupIterator): Int {
        return i.compareTo(other.index)
    }

}