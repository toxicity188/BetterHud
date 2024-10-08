package kr.toxicity.hud.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets

val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

fun JsonElement.save(file: File) {
    JsonWriter(FileWriter(file, StandardCharsets.UTF_8).buffered()).use {
        GSON.toJson(this, it)
    }
}
fun JsonElement.toByteArray(): ByteArray {
    val sb = StringBuilder()
    GSON.toJson(this, sb)
    return sb.toString().toByteArray(Charsets.UTF_8)
}

fun buildJsonArray(block: JsonArray.() -> Unit) = JsonArray().apply(block)
fun buildJsonObject(block: JsonObject.() -> Unit) = JsonObject().apply(block)

fun jsonArrayOf(vararg element: Any) = buildJsonArray {
    element.forEach {
        when (it) {
            is String -> add(it)
            is Char -> add(it)
            is Number -> add(it)
            is Boolean -> add(it)
            is JsonElement -> add(it)
            else -> throw RuntimeException("Unsupported type. ${it.javaClass.name}")
        }
    }
}

fun jsonObjectOf(vararg element: Pair<String, Any>) = buildJsonObject {
    element.forEach {
        addElementToJsonObject(it.first, it.second)
    }
}
private fun JsonObject.addElementToJsonObject(key: String, any: Any) {
    when (any) {
        is String -> addProperty(key, any)
        is Char -> addProperty(key, any)
        is Number -> addProperty(key, any)
        is Boolean -> addProperty(key, any)
        is JsonElement -> add(key, any)
        else -> throw RuntimeException("Unsupported type: ${any.javaClass.name}")
    }
}