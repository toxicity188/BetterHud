package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.util.sum

class PopupIteratorGroupImpl(private val dispose: Boolean): PopupIteratorGroup {
    private val list = ArrayList<PopupIterator>()

    override fun addIterator(iterator: PopupIterator) {
        val p = iterator.priority
        val map = list.map {
            it.index
        }.toSet()
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

    override fun next(): List<WidthComponent> {
        val iterator = list.iterator()
        var i = 0
        while (iterator.hasNext()) {
            val next = iterator.next()
            val index = next.index
            if (index < 0 || index > next.maxIndex || !next.available()) {
                list.subList(i, list.size).forEach {
                    it.index = (it.index - 1).coerceAtLeast(it.priority)
                }
                next.remove()
                iterator.remove()
            } else i++
        }
        val result = list.map {
            it.next()
        }.sum()
        if (dispose) list.clear()
        return result
    }

    override fun contains(name: String): Boolean {
        return list.any {
            it.name() == name
        }
    }
    override fun getIndex(): Int = list.size
}