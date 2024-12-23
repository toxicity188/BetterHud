package kr.toxicity.hud.util

import kr.toxicity.hud.configuration.HudConfiguration

fun <V : HudConfiguration> MutableMap<String, V>.putSync(name: String, v: () -> V) {
    val get = v()
    synchronized(this) {
        putIfAbsent(get.id, get)?.let {
            warn("Collision has been occurred in $name: ${it.id}")
        }
    }
}