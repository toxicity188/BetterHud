package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.image.ImageLocation
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.player.head.HeadRenderType
import kr.toxicity.hud.player.head.HudHead
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toLayoutAlign

class HeadLayout(
    s: String,
    yamlObject: YamlObject,
    loc: ImageLocation
) : HudLayout(loc, yamlObject) {
    val head: HudHead = yamlObject.get("name")?.asString().ifNull("name value not set: $s").let {
        PlayerHeadManager.getHead(it).ifNull("this head doesn't exist: $it in $s")
    }
    val type = HeadRenderType.valueOf(yamlObject.getAsString("type", "standard").uppercase())
    val align: LayoutAlign = when (type) {
        HeadRenderType.STANDARD -> yamlObject.get("align")?.asString().toLayoutAlign()
        HeadRenderType.FANCY -> LayoutAlign.CENTER
    }
}