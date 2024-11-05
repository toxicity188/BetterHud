package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.util.toSpaceComponent

//TODO Fix this
class BackgroundRenderer(
    val x: Int,
    private val component: BackgroundComponent
) {
    fun build(width: Int): WidthComponent {
        var comp = x.toSpaceComponent()
        var w = 0
        var children = component.x.toSpaceComponent() + component.start
        while (w < width) {
            children += component.center
            w += component.center.width
        }
        children += component.end
        comp += (-comp.width).toSpaceComponent() + children
        return comp
    }


    class BackgroundComponent(
        val x: Int,
        val start: WidthComponent,
        val center: WidthComponent,
        val end: WidthComponent
    )
}