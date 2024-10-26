package kr.toxicity.hud.placeholder

import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.update.UpdateEvent

fun interface ConditionBuilder {
    companion object {
        val alwaysTrue = ConditionBuilder {
            {
                true
            }
        }
    }
    fun build(updateEvent: UpdateEvent): (HudPlayer) -> Boolean

    infix fun and(other: ConditionBuilder) = ConditionBuilder result@ { updateEvent ->
        val build1 = this@ConditionBuilder.build(updateEvent)
        val build2 = other.build(updateEvent)
        ({ p ->
            build1(p) && build2(p)
        })
    }
    infix fun or(other: ConditionBuilder) = ConditionBuilder result@ { updateEvent ->
        val build1 = this@ConditionBuilder.build(updateEvent)
        val build2 = other.build(updateEvent)
        ({ p ->
            build1(p) || build2(p)
        })
    }
}