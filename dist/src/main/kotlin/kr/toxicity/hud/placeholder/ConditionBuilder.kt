package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent

interface ConditionBuilder {
    companion object {
        val alwaysTrue = object : ConditionBuilder {
            override fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean {
                return {
                    true
                }
            }
        }
    }
    fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean

    fun and(other: ConditionBuilder) = object : ConditionBuilder {
        override fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean {
            val build1 = this@ConditionBuilder.build(updateEvent)
            val build2 = other.build(updateEvent)
            return { p ->
                build1(p) && build2(p)
            }
        }
    }
    fun or(other: ConditionBuilder) = object : ConditionBuilder {
        override fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean {
            val build1 = this@ConditionBuilder.build(updateEvent)
            val build2 = other.build(updateEvent)
            return { p ->
                build1(p) || build2(p)
            }
        }
    }
}