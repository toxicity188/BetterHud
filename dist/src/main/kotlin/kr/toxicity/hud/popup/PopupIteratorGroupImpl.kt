package kr.toxicity.hud.popup

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.api.popup.PopupIterator
import kr.toxicity.hud.api.popup.PopupIteratorGroup
import kr.toxicity.hud.util.sum

class PopupIteratorGroupImpl: PopupIteratorGroup {
    private val list = ArrayList<PopupIterator>()
    override fun available(): Boolean {
        return list.isNotEmpty() && list.any() {
            it.available()
        }
    }

    override fun addIterator(iterator: PopupIterator) {
        list.add(iterator)
    }

    override fun next(): List<WidthComponent> {
        list.removeIf {
            !it.available()
        }
        return list.map {
            it.next()
        }.sum()
    }
    override fun getIndex(): Int = list.size
}