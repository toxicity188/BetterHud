package kr.toxicity.hud.util

import kr.toxicity.hud.configuration.HudConfiguration

fun <V : HudConfiguration> MutableMap<String, V>.putSync(name: String, k: String, v: () -> V) {
    val get = v()
    synchronized(this) {
        putIfAbsent(k, get)?.let {
            warn("Collision has been occurred in $name: ${it.path} and ${get.path}")
        }
    }
}