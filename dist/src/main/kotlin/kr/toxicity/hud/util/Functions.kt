package kr.toxicity.hud.util

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.ConfigManagerImpl
import java.net.http.HttpClient

fun interface Runner<T> : () -> T

fun <T> T?.ifNull(lazyMessage: () -> String): T & Any = this ?: throw RuntimeException(lazyMessage())

fun <T> httpClient(block: HttpClient.() -> T) = HttpClient.newHttpClient().use {
    runCatching {
        it.block()
    }
}

fun String.toEquation() = TEquation(this)

fun String?.toLayoutAlign(): LayoutAlign = if (this != null) LayoutAlign.valueOf(uppercase()) else LayoutAlign.LEFT

fun Throwable.handle(log: String) {
    handle(log) {
        warn(*it.toTypedArray())
    }
}

fun Throwable.handle(sender: BetterCommandSource, log: String) {
    handle(log) {
        synchronized(sender.audience()) {
            it.forEach(sender::info)
        }
    }
}

fun Throwable.handle(log: String, handler: (List<String>) -> Unit) {
    if (ConfigManagerImpl.debug()) {
        warn(
            "Stack trace:",
            stackTraceToString()
        )
    }
    handler(listOf(
        log,
        "Reason: ${message ?: javaClass.name}"
    ))
}

infix fun <T, R> ((T) -> R).memoize(initialValue: R): (T) -> R = this as? MemoizedFunction ?: MemoizedFunction(this, initialValue)

private class MemoizedFunction<T, R>(
    private val delegate: (T) -> R,
    private var value: R
) : (T) -> R {

    private var time = 0L

    @Synchronized
    override fun invoke(p1: T): R {
        val current = System.currentTimeMillis()
        if (current - time >= ConfigManagerImpl.tickSpeed * 50) {
            time = current
            value = delegate(p1)
        }
        return value
    }
}