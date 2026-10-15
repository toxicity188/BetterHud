package kr.toxicity.hud.animation

import kr.toxicity.hud.api.yaml.YamlObject

class AnimationTrack(
    val type: AnimationType,
    val frames: List<AnimationFrame>
) {
    companion object {
        val none = AnimationTrack(AnimationType.LOOP, listOf(AnimationFrame.identity))

        fun of(type: AnimationType, duration: Int, source: AnimationSource, quantizer: AnimationQuantizer = AnimationQuantizer()) = AnimationTrack(
            type,
            (0..<duration.coerceAtLeast(1)).map { quantizer.quantize(source.frame(it)) }
        )

        fun parse(section: YamlObject): AnimationTrack {
            val type = AnimationType.parse(section["type"]?.asString())
            val quantizer = AnimationQuantizer(
                section.getAsDouble("opacity-step", AnimationQuantizer.DEFAULT_OPACITY_STEP),
                section.getAsDouble("scale-step", AnimationQuantizer.DEFAULT_SCALE_STEP)
            )
            val keyframes = section["keyframes"]?.asArray()?.mapNotNull { element ->
                runCatching { element.asObject() }.getOrNull()?.let(Keyframe::parse)
            }
            return if (keyframes != null) {
                val animation = KeyframeAnimation(keyframes)
                of(type, section.getAsInt("duration", animation.duration), animation, quantizer)
            } else of(type, section.getAsInt("duration", 20), EquationAnimation(section), quantizer)
        }
    }

    init {
        require(frames.isNotEmpty()) { "animation frames cannot be empty" }
    }

    val states: List<AnimationFrame> = frames.distinct()
    private val stateOfFrame = IntArray(frames.size) { states.indexOf(frames[it]) }
    private val frameIndex = IntArray(frames.size) { it }

    val isStatic get() = states.size == 1 && states.single().isIdentity
    val duration get() = frames.size

    fun frameAt(t: Long) = frames[type.choose(frameIndex, t)]

    fun <T> bake(baker: (AnimationFrame) -> T) = Animated(type, stateOfFrame, states.map(baker))
}
