package kr.toxicity.hud.renderer.bake

import com.google.gson.JsonArray
import net.kyori.adventure.key.Key

interface ElementHost {
    val hostKind: String
    val hostName: String
    val imageKey: Key
    val newChar: String
    val json: JsonArray?
    fun getOrCreateSpace(int: Int): String
    fun nextTextIndex(): Int
    fun releaseJson()
}
