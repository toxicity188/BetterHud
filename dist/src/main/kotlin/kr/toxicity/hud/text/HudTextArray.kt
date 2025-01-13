package kr.toxicity.hud.text

import com.google.gson.JsonArray

class HudTextArray(
    val file: String,
    val chars: JsonArray,
    val height: Double,
    val ascent: (Double) -> Int
)