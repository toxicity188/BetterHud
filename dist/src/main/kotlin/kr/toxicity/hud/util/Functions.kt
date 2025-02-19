package kr.toxicity.hud.util

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.ConfigManagerImpl

fun interface Runner<T> : () -> T

fun <T> T?.ifNull(message: String): T & Any {
    return this ?: throw RuntimeException(message)
}

fun String.toEquation() = TEquation(this)

fun String?.toLayoutAlign(): LayoutAlign = if (this != null) LayoutAlign.valueOf(uppercase()) else LayoutAlign.LEFT

fun <R> runWithExceptionHandling(sender: BetterCommandSource, message: String, block: () -> R) = runCatching(block).onFailure {
    synchronized(sender) {
        sender.warn(message)
        sender.warn("Reason: ${it.message ?: it.javaClass.name}")
    }
    if (ConfigManagerImpl.debug()) {
        warn(
            "Stack trace:",
            it.stackTraceToString()
        )
    }
}

fun <T, R> T.runWithExceptionHandling(sender: BetterCommandSource, message: String, block: T.() -> R) = runCatching(block).onFailure {
    synchronized(sender) {
        sender.warn(message)
        sender.warn("Reason: ${it.message ?: it.javaClass.name}")
    }
    if (ConfigManagerImpl.debug()) {
        warn(
            "Stack trace:",
            it.stackTraceToString()
        )
    }
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