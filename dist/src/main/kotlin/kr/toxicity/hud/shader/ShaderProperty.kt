package kr.toxicity.hud.shader

import kr.toxicity.hud.api.yaml.YamlArray

enum class ShaderProperty {
    WAVE,
    RAINBOW,
    TINY_RAINBOW
    ;

    val id get() = 1 shl ordinal

    companion object {
        fun properties(yamlArray: YamlArray?): Int {
            var result = 0
            yamlArray?.forEach {
                result = result or valueOf(it.asString().uppercase()).id
            }
            return result
        }
    }
}