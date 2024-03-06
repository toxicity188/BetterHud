package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.util.sum

class PopupIteratorGroupImpl: PopupIteratorGroup {
    private val list = ArrayList<PopupIterator>()
    override fun available(): Boolean {
        return list.isNotEmpty() && list.any {
            it.available()
        }
    }

    override fun addIterator(iterator: PopupIterator) {
        iterator.index = index
        list.add(iterator)
    }

    override fun next(): List<WidthComponent> {
        val iterator = list.iterator()
        var i = 0
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (!next.available()) {
                list.subList(i, list.size).forEach {
                    it.index --
                }
                iterator.remove()
            } else i++
        }
        return list.map {
            it.next()
        }.sum()
    }

    override fun contains(name: String): Boolean {
        return list.any {
            it.name() == name
        }
    }
    override fun getIndex(): Int = list.size
}