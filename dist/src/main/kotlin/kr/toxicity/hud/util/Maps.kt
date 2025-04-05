package kr.toxicity.hud.util

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import kr.toxicity.hud.configuration.HudConfiguration

fun <V : HudConfiguration> MutableMap<String, V>.putSync(name: String, v: () -> V) {
    val get = v()
    synchronized(this) {
        putIfAbsent(get.id, get)?.let {
            warn("Collision has been occurred in $name: ${it.id}")
        }
    }
}

fun Map<Int, Int>.toIntMap() = Int2IntArrayMap(this)
fun <V> Map<Int, V>.toIntKeyMap() = Int2ObjectArrayMap<V>(this)

fun intMapOf() = Int2IntOpenHashMap()
fun <V> intKeyMapOf() = Int2ObjectOpenHashMap<V>()

val <V> IntKeyMap<V>.intEntries: ObjectSet<Int2ObjectMap.Entry<V>>
    get() = int2ObjectEntrySet()