package kr.toxicity.hud.util

import java.util.concurrent.CompletableFuture

fun <T> List<T>.split(splitSize: Int): List<List<T>> {
    val result = ArrayList<List<T>>()
    var index = 0
    while (index < size) {
        val subList = subList(index, (index + splitSize).coerceAtMost(size))
        if (subList.isNotEmpty()) result.add(subList)
        index += splitSize
    }
    return result
}

fun <T> List<List<T>>.sum(): List<T> {
    val result = ArrayList<T>()
    forEach {
        result.addAll(it)
    }
    return result
}

fun <T> Collection<T>.forEachAsync(block: (T) -> Unit, callback: () -> Unit) {
    if (isNotEmpty()) {
        val current = TaskIndex(size)
        forEach {
            CompletableFuture.runAsync {
                runCatching {
                    block(it)
                }.onFailure { e ->
                    e.printStackTrace()
                }
                synchronized(current) {
                    if (++current.current == current.max) {
                        callback()
                    }
                }
            }
        }
    } else callback()
}