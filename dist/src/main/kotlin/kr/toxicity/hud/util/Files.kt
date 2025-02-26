package kr.toxicity.hud.util

import kr.toxicity.hud.api.yaml.YamlObject
import java.io.File

fun File.subFolder(dir: String) = File(this, dir).apply {
    if (!exists()) mkdir()
}

fun File.subFile(name: String) = File(this, name).apply {
    if (!exists()) createNewFile()
}

fun File.ifNotExist(lazyMessage: () -> String) = takeIf { exists() }.ifNull(lazyMessage)

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

fun File.mapAllFolder() = mutableListOf<File>().apply {
    forEachAllFolder(this::add)
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