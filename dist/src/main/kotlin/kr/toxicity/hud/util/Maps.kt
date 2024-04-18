package kr.toxicity.hud.util

fun <K, V> MutableMap<K, V>.putSync(k: K, v: () -> V) {
    val get = v()
    synchronized(this) {
        put(k, get)
    }
}