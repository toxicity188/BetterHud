package kr.toxicity.hud.util

import kr.toxicity.hud.equation.TEquation

fun <T> T?.ifNull(message: String): T & Any {
    return this ?: throw RuntimeException(message)
}

fun String.toEquation() = TEquation(this)