package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.util.sum

class PopupIteratorGroupImpl(private val dispose: Boolean): PopupIteratorGroup {
    private val list = ArrayList<PopupIterator>()
    override fun available(): Boolean {
        return list.isNotEmpty() && list.any {
            it.available()
        }
    }

    override fun addIterator(iterator: PopupIterator) {
        iterator.index = if (iterator.priority >= 0 && list.none {
            it.index == iterator.priority
        }) iterator.priority else run {
            var i = 0
            val map = list.map {
                it.index
            }
            while (map.contains(i)) i++
            i
        }
        list.add(iterator)
    }

    override fun clear() {
        list.clear()
    }

    override fun next(): List<WidthComponent> {
        val iterator = list.iterator()
        var i = 0
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (!next.available()) {
                list.subList(i, list.size).forEach {
                    it.index = (it.index - 1).coerceAtLeast(it.priority)
                }
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