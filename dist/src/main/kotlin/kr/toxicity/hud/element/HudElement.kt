package kr.toxicity.hud.element

import kr.toxicity.hud.configuration.HudConfiguration
import kr.toxicity.hud.placeholder.ConditionSource

interface HudElement : ConditionSource, HudConfiguration {
    val name: String
}