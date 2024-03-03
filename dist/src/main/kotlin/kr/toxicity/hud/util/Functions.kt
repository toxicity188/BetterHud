package kr.toxicity.hud.util

fun <T> T?.ifNull(message: String): T & Any {
    return this ?: throw RuntimeException(message)
}