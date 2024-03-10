package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.api.popup.PopupSortType
import kr.toxicity.hud.util.sum

class PopupIteratorGroupImpl(private val dispose: Boolean): PopupIteratorGroup {
    private val list = ArrayList<PopupIterator>()

    override fun addIterator(iterator: PopupIterator) {
        val i = if (iterator.priority >= 0 && list.none {
                it.index == iterator.priority
            }) iterator.priority else run {
            var i = 0
            val map = list.map {
                it.index
            }
            while (map.contains(i)) i++
            i
        }
        iterator.index = i
        if (iterator.sortType == PopupSortType.FIRST) {
            var t = 0
            var biggest = i
            val more = list.filter {
                it.index >= i
            }
            while (t < more.size && more[t].index >= biggest) {
                more[t].index++
                biggest = more[t].index
                t++
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
            if (next.index > next.maxIndex || !next.available()) {
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