package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent
import kr.toxicity.hud.api.yaml.YamlObject
import kr.toxicity.hud.util.ifNull
import kr.toxicity.hud.util.toConditions
import kr.toxicity.hud.util.toTextColor
import net.kyori.adventure.text.format.TextColor

fun interface ColorOverride : (HudPlayer) -> TextColor? {

    fun interface Builder : (UpdateEvent) -> (HudPlayer) -> TextColor? {
        operator fun plus(other: Builder): Builder {
            return Builder new@ {
                val b1 = this@Builder(it)
                val b2 = other(it)
                ColorOverride { p ->
                    b1(p) ?: b2(p)
                }
            }
        }
    }

    class YamlBuilder : Builder {
        private val pair = ArrayList<Pair<ConditionBuilder, TextColor?>>()
        var defaultColor: TextColor? = null

        private fun conditional(builder: ConditionBuilder, color: TextColor?) {
            pair += builder to color
        }

        fun conditional(yaml: YamlObject, source: PlaceholderSource) {
            conditional(
                yaml.toConditions(source),
                yaml["color"]
                    .ifNull { "'color' section not found." }
                    .asString()
                    .toTextColor()
            )
        }

        override fun invoke(p1: UpdateEvent): ColorOverride {
            val built = pair.map { (a, b) ->
                (a build p1) to b
            }
            return ColorOverride {
                built.firstNotNullOfOrNull { (a, b) ->
                    if (a(it)) b else null
                } ?: defaultColor
            }
        }
    }

    companion object {
        val empty = Builder {
            ColorOverride {
                null
            }
        }
        fun builder(yaml: YamlObject, source: PlaceholderSource) : Builder {
            return YamlBuilder().apply {
                yaml.forEach {
                    conditional(it.value.asObject(), source)
                }
                defaultColor = yaml["default-color"]?.asString()?.toTextColor()
            }
        }
    }

    operator fun plus(other: ColorOverride) = ColorOverride new@ {
        this@ColorOverride(it) ?: other(it)
    }
}