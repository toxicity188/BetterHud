package kr.toxicity.hud.bootstrap.bukkit.util

import org.bukkit.entity.LivingEntity

val LivingEntity.maximumHealth
    get() = getAttribute(ATTRIBUTE_MAX_HEALTH)!!.value