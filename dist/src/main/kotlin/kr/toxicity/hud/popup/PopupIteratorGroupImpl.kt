package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupSortType
import java.util.*

class PopupIteratorGroupImpl(
    private val dispose: Boolean,
): PopupIteratorGroup {
    private val sourceSet = TreeSet<PopupIterator>()

    override fun addIterator(iterator: PopupIterator) {
        synchronized(sourceSet) {
            if (iterator.isUnique && contains(iterator.name())) return
            val p = iterator.priority
            val map = HashSet<Int>()
            val loop = sourceSet.iterator()
            while (loop.hasNext()) {
                val next = loop.next()
                if (next.markedAsRemoval()) next.remove()
                else map.add(next.index)
            }
            val i = if (iterator.index < 0) when (iterator.sortType) {
                PopupSortType.FIRST -> if (p >= 0) p else 0
                PopupSortType.LAST -> if (p >= 0) p else run {
                    var i = 0
                    while (map.contains(i)) i++
                    i
                }
            } else iterator.index
            iterator.index = i
            if (map.contains(i)) {
                var t = 0
                var biggest = i
                val more = sourceSet.filter {
                    it.index >= i
                }
                val newValue = ArrayList<PopupIterator>(more.size)
                while (t < more.size && more[t].index >= biggest) {
                    val get = more[t++]
                    if (sourceSet.remove(get)) {
                        biggest = ++get.index
                        newValue.add(get)
                    }
                }
                if (newValue.isNotEmpty()) sourceSet.addAll(newValue)
            }
            sourceSet.add(iterator)
        }
    }

    override fun clear() {
        synchronized(sourceSet) {
            sourceSet.forEach {
                it.remove()
            }
            sourceSet.clear()
        }
    }

    private fun checkCondition(iterator: PopupIterator): Boolean {
        if (iterator.markedAsRemoval()) {
            return false
        }
        if (iterator.index > iterator.maxIndex) {
            if (!iterator.canSave() || (iterator.alwaysCheckCondition() && !iterator.available())) {
                return false
            }
        }
        if (iterator.index < 0 || !iterator.available()) return false
        return true
    }

    override fun next(): List<WidthComponent> {
        synchronized(sourceSet) {
            val send = ArrayList<PopupIterator>()
            val result = ArrayList<WidthComponent>()
            sourceSet.forEach { next ->
                if (checkCondition(next)) {
                    result.addAll(next.next())
                    send.add(next)
                }
                else next.remove()
            }
            if (!dispose) send.forEach {
                addIterator(it)
            }
            return result
        }
    }

    override fun contains(name: String): Boolean {
        return synchronized(sourceSet) {
            sourceSet.any {
                it.name() == name
            }
        }
    }
    override fun getIndex(): Int = sourceSet.size
}