package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.util.sum
import java.util.Collections
import java.util.Comparator
import java.util.TreeSet

class PopupIteratorGroupImpl(
    private val dispose: Boolean,
): PopupIteratorGroup {
    private var list = Collections.synchronizedSet(TreeSet<PopupIterator>(Comparator.naturalOrder()))

    override fun addIterator(iterator: PopupIterator) {
        if (iterator.isUnique && contains(iterator.name())) return
        val p = iterator.priority
        val map = HashSet<Int>()
        val loop = list.iterator()
        while (loop.hasNext()) {
            val next = loop.next()
            if (next.markedAsRemoval()) next.remove()
            else map.add(next.index)
        }
        val i = when (iterator.sortType) {
            PopupSortType.FIRST -> if (p >= 0) p else 0
            PopupSortType.LAST -> if (p >= 0) p else run {
                var i = 0
                while (map.contains(i)) i++
                i
            }
        }
        iterator.index = i
        if (map.contains(i)) {
            var t = 0
            var biggest = i
            val more = list.filter {
                it.index >= i
            }
            while (t < more.size && more[t].index >= biggest) {
                biggest = more[t++].index++
            }
        }
        list.add(iterator)
    }

    override fun clear() {
        list.forEach {
            it.remove()
        }
        list.clear()
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
        val copy = list
        list = Collections.synchronizedSet(TreeSet(Comparator.naturalOrder()))
        val send = ArrayList<PopupIterator>()
        copy.forEach { next ->
            if (checkCondition(next)) send.add(next)
            else next.remove()
        }
        val result = ArrayList<WidthComponent>()
        if (!dispose) send.forEach {
            addIterator(it)
            if (it.index <= it.maxIndex) result.addAll(it.next())
        }
        return result
    }

    override fun contains(name: String): Boolean {
        return list.any {
            it.name() == name
        }
    }
    override fun getIndex(): Int = list.size
}