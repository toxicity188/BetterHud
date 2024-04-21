package kr.toxicity.hud.util

import kr.toxicity.hud.configuration.HudConfiguration
import java.util.Collections
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

private val CACHE_MAP = ConcurrentHashMap<String, MutableSet<String>>()

private fun getCache(name: String) = CACHE_MAP.computeIfAbsent(name) {
    Collections.synchronizedSet(HashSet())
}

fun <V: HudConfiguration> MutableMap<String, V>.putSync(name: String, k: String, v: () -> V) {
    val cache = getCache(name)
    fun warn() = warn("Name collision found: $k in $name")
    fun remove() {
        cache.remove(k)
        if (cache.isEmpty()) CACHE_MAP.remove(name)
    }
    if (!cache.add(k)) return warn()
    runCatching {
        synchronized(this) {
            get(k)
        }?.let {
            warn()
            return remove()
        }
        val get = v()
        synchronized(this) {
            putIfAbsent(k, get)?.let {
                warn("Error has been occurred: ${it.path} and ${get.path}")
            }
            remove()
        }
    }.onFailure {
        remove()
        throw it
    }
}
