package kr.toxicity.hud.animation

import kr.toxicity.hud.api.yaml.YamlObject

data class Keyframe(
    val t: Int,
    val x: Double? = null,
    val y: Double? = null,
    val opacity: Double? = null,
    val scale: Double? = null,
    val visible: Boolean? = null,
    val easing: Easing = Easing.LINEAR
) {
    companion object {
        fun parse(section: YamlObject): Keyframe = Keyframe(
            section["t"]?.asInt() ?: throw RuntimeException("keyframe requires t"),
            section["x"]?.asDouble(),
            section["y"]?.asDouble(),
            section["opacity"]?.asDouble(),
            section["scale"]?.asDouble(),
            section["visible"]?.asBoolean(),
            Easing.parse(section["easing"]?.asString())
        )
    }

    init {
        require(t >= 0) { "keyframe t cannot be negative: $t" }
    }
}
