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

fun <T> List<T>.forEachAsync(block: (Int, T) -> Unit, callback: () -> Unit) {
    val current = TaskIndex(size)
    if (isNotEmpty()) forEach {
        CompletableFuture.runAsync {
            block(current.current, it)
        }.thenAccept {
            synchronized(current) {
                if (++current.current == current.max) {
                    callback()
                }
            }
        }
    } else callback()
}

fun <T> Set<T>.forEachAsync(block: (Int, T) -> Unit, callback: () -> Unit) {
    val current = TaskIndex(size)
    if (isNotEmpty()) forEach {
        CompletableFuture.runAsync {
            block(current.current, it)
        }.thenAccept {
            synchronized(current) {
                if (++current.current == current.max) {
                    callback()
                }
            }
        }
    } else callback()
}