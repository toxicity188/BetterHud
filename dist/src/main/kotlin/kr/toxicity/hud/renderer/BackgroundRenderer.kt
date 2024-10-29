package kr.toxicity.hud.renderer

import kr.toxicity.hud.api.component.WidthComponent
import kr.toxicity.hud.util.toSpaceComponent

class BackgroundRenderer(
    val x: Int,
    private val lists: List<BackgroundComponent>
) {

    fun build(line: Int, width: Int): WidthComponent {
        var comp = x.toSpaceComponent()
        var w = 0
        for (i in 0..<line) {
            val target = getLine(i)
            var children = target.x.toSpaceComponent() + target.start
            while (w < width) {
                children += target.center
                w += target.center.width
            }
            children += target.end
            comp += (-comp.width).toSpaceComponent() + children
        }
        return comp
    }

    private fun getLine(line: Int) = if (line == 0) lists.first() else if (line == lists.lastIndex) lists.last() else lists[1]

    class BackgroundComponent(
        val x: Int,
        val start: WidthComponent,
        val center: WidthComponent,
        val end: WidthComponent
    )
}