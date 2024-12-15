package kr.toxicity.hud.util

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

fun <T> List<T>.split(splitSize: Int): List<List<T>> {
    val result = ArrayList<List<T>>()
    var index = 0
    while (index < size) {
        val subList = subList(index, (index + splitSize).coerceAtMost(size))
        if (subList.isNotEmpty()) result += subList
        index += splitSize
    }
    return result
}

fun <T> List<List<T>>.sum(): List<T> {
    val result = ArrayList<T>()
    forEach {
        result += it
    }
    return result
}

fun <T> Collection<T>.forEachAsync(block: (T) -> Unit) {
    toList().forEachAsync(block)
}

fun <T> MutableCollection<T>.removeIfSync(block: (T) -> Boolean) {
    synchronized(this) {
        val iterator = iterator()
        synchronized(iterator) {
            while (iterator.hasNext()) {
                if (block(iterator.next())) iterator.remove()
            }
        }
    }
}


fun <T> List<T>.forEachAsync(block: (T) -> Unit) {
    forEachAsync({ i -> i }, block)
}
fun <T> List<T>.forEachAsync(multiplier: (Int) -> Int, block: (T) -> Unit) {
    if (isNotEmpty()) {
        val available = multiplier(Runtime.getRuntime().availableProcessors())
            .coerceAtLeast(4)
            .coerceAtMost(256)
        val tasks = if (available >= size) {
            map {
                {
                    block(it)
                }
            }
        } else {
            val queue = ArrayList<() -> Unit>()
            var i = 0
            val add = (size.toDouble() / available).toInt()
            while (i <= size) {
                val get = subList(i, (i + add).coerceAtMost(size))
                queue += {
                    get.forEach { t ->
                        block(t)
                    }
                }
                i += add
            }
            queue
        }
        try {
            Executors.newFixedThreadPool(tasks.size).use { pool ->
                CompletableFuture.allOf(
                    *tasks.map {
                        CompletableFuture.runAsync({
                            it()
                        }, pool)
                    }.toTypedArray()
                ).join()
            }
        } catch (error: OutOfMemoryError) {
            warn(
                "Async task failed!",
                "You have to set your Linux max thread limit!",
                "",
                "Stack trace:",
                error.stackTraceToString()
            )
        }
    }
}