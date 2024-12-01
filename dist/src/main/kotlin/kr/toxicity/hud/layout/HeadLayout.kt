package kr.toxicity.hud.layout

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.location.PixelLocation
import kr.toxicity.hud.manager.PlayerHeadManager
import kr.toxicity.hud.player.head.HeadRenderType
import kr.toxicity.hud.element.HeadElement
import kr.toxicity.hud.shader.HudShader
import kr.toxicity.hud.shader.ShaderGroup
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toLayoutAlign

interface HeadLayout : HudLayout<HeadElement> {
    val type: HeadRenderType
    val align: LayoutAlign

    fun identifier(shader: HudShader, ascent: Int): HudLayout.Identifier {
        return ShaderGroup(shader, source.name, ascent)
    }

    class Impl(
        override val source: HeadElement,
        group: LayoutGroup,
        yamlObject: YamlObject,
        loc: PixelLocation
    ) : HeadLayout, HudLayout<HeadElement> by HudLayout.Impl(source, group, loc, yamlObject) {
        constructor(
            s: String,
            group: LayoutGroup,
            yamlObject: YamlObject,
            loc: PixelLocation
        ): this(
            yamlObject["name"]?.asString().ifNull("name value not set: $s").let {
                PlayerHeadManager.getHead(it).ifNull("this head doesn't exist: $it in $s")
            },
            group,
            yamlObject,
            loc
        )
        override val type = HeadRenderType.valueOf(yamlObject.getAsString("type", "standard").uppercase())
        override val align: LayoutAlign = when (type) {
            HeadRenderType.STANDARD -> yamlObject["align"]?.asString().toLayoutAlign()
            HeadRenderType.FANCY -> LayoutAlign.CENTER
        }
    }
}