package kr.toxicity.hud.util

import org.bukkit.configuration.ConfigurationSection
import java.io.File

fun File.subFolder(dir: String) = File(this, dir).apply {
    if (!exists()) mkdir()
}

fun File.subFile(name: String) = File(this, name).apply {
    if (!exists()) createNewFile()
}

fun File.ifNotExist(message: String) = apply {
    if (!exists()) throw RuntimeException(message)
}

fun File.clearFolder() = apply {
    deleteRecursively()
    mkdir()
}

fun File.forEach(block: (File) -> Unit) {
    listFiles()?.forEach(block)
}

fun File.forEachAllFolder(block: (File) -> Unit) {
    if (isDirectory) forEach {
        it.forEachAllFolder(block)
    } else {
        block(this)
    }
}

fun File.forEachAllYaml(block: (File, String, ConfigurationSection) -> Unit) {
    forEachAllFolder {
        if (it.extension == "yml") {
            runCatching {
                it.toYaml().forEachSubConfiguration { s, configurationSection ->
                    block(it, s, configurationSection)
                }
            }.onFailure { e ->
                warn("Unable to load this yml file: ${it.name}")
                warn("Reason: ${e.message}")
            }
        }
    }
}