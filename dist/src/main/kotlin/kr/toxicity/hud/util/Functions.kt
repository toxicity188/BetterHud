package kr.toxicity.hud.util

fun <T> T?.ifNull(message: String): T {
    return this ?: throw RuntimeException(message)
}