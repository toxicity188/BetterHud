package kr.toxicity.hud.text

import com.google.gson.JsonArray

data class HudTextArray(
    val file: String,
    val chars: JsonArray,
    val height: Int
)
