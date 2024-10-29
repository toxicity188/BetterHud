package kr.toxicity.hud.util

import java.lang.ref.WeakReference

class ValueViewer<K, V> : (K) -> V? {

    private val refs = ArrayList<(K) -> V?>()

    fun addMap(vararg map: Map<K, V>): ValueViewer<K, V> {
        val mapRef = map.map {
            WeakReference(it)
        }
        for (weakReference in mapRef) {
            refs.add {
                weakReference.get()?.get(it)
            }
        }
        return this
    }
    fun addFunction(vararg function: (K) -> V?): ValueViewer<K, V> {
        refs.addAll(function)
        return this
    }

    override fun invoke(p1: K): V? {
        return refs.firstNotNullOfOrNull {
            it(p1)
        }
    }
}