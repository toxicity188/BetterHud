package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.yaml.YamlObjectImpl

interface PlaceholderSource {
    val placeholderOption: YamlObject
    val stringPlaceholderFormat: YamlObject

    companion object {
        val empty = Impl(
            YamlObjectImpl.empty,
            YamlObjectImpl.empty
        )
    }

    class Impl(
        override val placeholderOption: YamlObject,
        override val stringPlaceholderFormat: YamlObject
    ) : PlaceholderSource {
        constructor(source: YamlObject): this(
            source["placeholder-option"]?.asObject() ?: YamlObjectImpl.empty,
            source["placeholder-string-format"]?.asObject() ?: YamlObjectImpl.empty
        )
    }
}