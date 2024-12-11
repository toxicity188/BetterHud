package kr.toxicity.hud.util

import kr.toxicity.hud.api.yaml.YamlObject
import java.io.File

fun File.subFolder(dir: String) = File(this, dir).apply {
    if (!exists()) mkdir()
}

fun File.subFile(name: String) = File(this, name).apply {
    if (!exists()) createNewFile()
}

fun File.ifNotExist(message: String) = takeIf { exists() }.ifNull(message)

fun File.ifNotExist(messageCreator: File.() -> String) = apply {
    if (!exists()) throw RuntimeException(messageCreator())
}

fun File.forEach(block: (File) -> Unit) {
    listFiles()?.sortedBy {
        it.name
    }?.forEach(block)
}

fun File.forEachAllFolder(block: (File) -> Unit) {
    if (isDirectory) forEach {
        it.forEachAllFolder(block)
    } else {
        block(this)
    }
}


fun YamlObject.forEachSubConfiguration(block: (String, YamlObject) -> Unit) {
    forEach {
        val v = it.value
        if (v is YamlObject) block(it.key, v)
    }
}
fun <T> YamlObject.mapSubConfiguration(block: (String, YamlObject) -> T) = mapNotNull {
    val v = it.value
    if (v is YamlObject) block(it.key, v) else null
}