package kr.toxicity.hud.util

import kr.toxicity.command.BetterCommandSource
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.api.version.MinecraftVersion
import kr.toxicity.hud.equation.TEquation
import kr.toxicity.hud.layout.enums.LayoutAlign
import kr.toxicity.hud.manager.ConfigManagerImpl
import org.semver4j.Semver
import java.net.http.HttpClient
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

fun interface Runner<T> : () -> T

fun <T> T?.ifNull(lazyMessage: () -> String): T & Any = this ?: throw RuntimeException(lazyMessage())

private val CLIENT = HttpClient.newBuilder()
    .executor(Executors.newVirtualThreadPerTaskExecutor())
    .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
    .build()

fun <T> httpClient(block: HttpClient.() -> T) = runCatching {
    CLIENT.block()
}

fun String.toSemver() = Semver.coerce(this).ifNull { "Cannot parse this semver: $this" }
fun String.toMinecraftVersion() = MinecraftVersion(this)

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
    val list = mutableListOf(
        log,
        "Reason: ${message ?: javaClass.name}"
    )
    if (ConfigManagerImpl.debug()) {
        list += listOf(
            "Stack trace:",
            stackTraceToString()
        )
    }
    handler(list)
}

fun <T> Result<T>.handleFailure(lazyMessage: () -> String) = onFailure { throwable ->
    throwable.handle(lazyMessage())
}
fun <T> Result<T>.handleFailure(info: ReloadInfo, lazyMessage: () -> String) = handleFailure(info.sender, lazyMessage)
fun <T> Result<T>.handleFailure(source: BetterCommandSource, lazyMessage: () -> String) = onFailure { throwable ->
    throwable.handle(source, lazyMessage())
}