package kr.toxicity.hud.util

class OverridableSet<K, V>(
    private val keyMapper: (V) -> K,
    mapCreator: () -> MutableMap<K, V> = {
        HashMap()
    }
): MutableSet<V> {
    private val map = mapCreator()

    override fun add(element: V): Boolean {
        map[keyMapper(element)] = element
        return true
    }

    override val size: Int
        get() = map.size

    override fun clear() = map.clear()

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun iterator(): MutableIterator<V> = map.values.iterator()

    override fun retainAll(elements: Collection<V>): Boolean = map.values.retainAll(elements.toSet())
    override fun removeAll(elements: Collection<V>): Boolean = map.values.removeAll(elements.toSet())

    override fun remove(element: V): Boolean = map.remove(keyMapper(element)) != null

    override fun containsAll(elements: Collection<V>): Boolean = elements.all {
        map.containsKey(keyMapper(it))
    }

    override fun contains(element: V): Boolean = map.containsKey(keyMapper(element))

    override fun addAll(elements: Collection<V>): Boolean = elements.any {
        add(it)
    }
}