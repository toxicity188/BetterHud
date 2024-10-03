package kr.toxicity.hud.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
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