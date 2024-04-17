package kr.toxicity.hud.util

fun <K, V> Map<K, V>.forEachAsync(block: (Int, Map.Entry<K, V>) -> Unit, callback: () -> Unit) {
    val current = TaskIndex(size)
    forEach {
        asyncTask {
            block(current.current, it)
            synchronized(current) {
                if (++current.current == current.max) {
                    callback()
                }
            }
        }
    }
}
