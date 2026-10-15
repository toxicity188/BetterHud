package kr.toxicity.hud.animation

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

enum class Easing {
    LINEAR {
        override fun apply(k: Double) = k
    },
    STEP {
        override fun apply(k: Double) = if (k >= 1.0) 1.0 else 0.0
    },
    IN_QUAD {
        override fun apply(k: Double) = k * k
    },
    OUT_QUAD {
        override fun apply(k: Double) = 1 - (1 - k) * (1 - k)
    },
    IN_OUT_QUAD {
        override fun apply(k: Double) = if (k < 0.5) 2 * k * k else 1 - (-2 * k + 2).pow(2) / 2
    },
    IN_CUBIC {
        override fun apply(k: Double) = k * k * k
    },
    OUT_CUBIC {
        override fun apply(k: Double) = 1 - (1 - k).pow(3)
    },
    IN_OUT_CUBIC {
        override fun apply(k: Double) = if (k < 0.5) 4 * k * k * k else 1 - (-2 * k + 2).pow(3) / 2
    },
    OUT_BACK {
        override fun apply(k: Double): Double {
            val c1 = 1.70158
            val c3 = c1 + 1
            return 1 + c3 * (k - 1).pow(3) + c1 * (k - 1).pow(2)
        }
    },
    OUT_ELASTIC {
        override fun apply(k: Double): Double = when {
            k <= 0.0 -> 0.0
            k >= 1.0 -> 1.0
            else -> 2.0.pow(-10 * k) * sin((k * 10 - 0.75) * (2 * PI / 3)) + 1
        }
    },
    OUT_BOUNCE {
        override fun apply(k: Double): Double {
            val n1 = 7.5625
            val d1 = 2.75
            return when {
                k < 1 / d1 -> n1 * k * k
                k < 2 / d1 -> (k - 1.5 / d1).let { n1 * it * it + 0.75 }
                k < 2.5 / d1 -> (k - 2.25 / d1).let { n1 * it * it + 0.9375 }
                else -> (k - 2.625 / d1).let { n1 * it * it + 0.984375 }
            }
        }
    },
    IN_OUT_SINE {
        override fun apply(k: Double) = -(cos(PI * k) - 1) / 2
    }
    ;

    abstract fun apply(k: Double): Double

    fun interpolate(from: Double, to: Double, k: Double) = from + (to - from) * apply(k.coerceIn(0.0, 1.0))

    companion object {
        fun parse(name: String?): Easing = if (name == null) LINEAR else runCatching {
            valueOf(name.uppercase().replace('-', '_'))
        }.getOrElse { throw RuntimeException("This easing doesn't exist: $name") }
    }
}
