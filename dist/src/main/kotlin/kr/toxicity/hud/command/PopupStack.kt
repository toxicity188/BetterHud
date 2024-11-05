package kr.toxicity.hud.command

import kr.toxicity.hud.api.popup.Popup
import java.util.Collections

class PopupStack(
    private val popupList: Collection<Popup>
) : Iterable<Popup> {
    override fun iterator(): Iterator<Popup> = Collections.unmodifiableCollection(popupList).iterator()
}