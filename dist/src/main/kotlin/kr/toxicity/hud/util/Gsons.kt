package kr.toxicity.hud.util

import com.google.gson.*
import com.google.gson.stream.JsonWriter
import java.io.File
import java.util.*

val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

fun JsonElement.save(file: File) {
    JsonWriter(file.bufferedWriter()).use {
        GSON.toJson(this, it)
    }
}
fun JsonElement.toByteArray(): ByteArray {
    val sb = StringBuilder()
    GSON.toJson(this, sb)
    return sb.toString().toByteArray(Charsets.UTF_8)
}

fun buildJsonArray(capacity: Int = 10, block: JsonArray.() -> Unit) = JsonArray(capacity).apply(block)
fun buildJsonObject(block: JsonObject.() -> Unit) = JsonObject().apply(block)

fun jsonArrayOf(vararg element: Any) = buildJsonArray {
    element.forEach {
        add(it.toJsonElement())
    }
}

fun jsonObjectOf(vararg element: Pair<String, Any>) = buildJsonObject {
    element.forEach {
        add(it.first, it.second.toJsonElement())
    }
}

fun Any.toJsonElement(): JsonElement = when (this) {
    is String -> JsonPrimitive(this)
    is Char -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is JsonElement -> this
    is List<*> -> run {
        val map = mapNotNull {
            it?.toJsonElement()
        }
        buildJsonArray(map.size) {
            map.forEach {
                add(it)
            }
        }
    }
    is Map<*, *> -> buildJsonObject {
        forEach {
            add(it.key?.toString() ?: return@forEach, it.value?.toJsonElement() ?: return@forEach)
        }
    }
    else -> throw RuntimeException("Unsupported type: ${javaClass.name}")
}

fun JsonElement.toBase64String(): String = Base64.getEncoder().encodeToString(GSON.toJson(this).toByteArray())
fun String.toBase64Json(): JsonElement = JsonParser.parseString(Base64.getDecoder().decode(this).toString(Charsets.UTF_8))