package kr.toxicity.hud.util

import net.kyori.adventure.audience.Audience
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


fun File.forEachAsync(block: (File) -> Unit) {
    listFiles()?.toList()?.forEachAsync(block)
}

fun File.forEachAllYaml(sender: Audience, block: (File, String, ConfigurationSection) -> Unit) {
    forEachAllFolder {
        if (it.extension == "yml") {
            runWithExceptionHandling(sender, "Unable to load this yml file: ${it.name}") {
                it.toYaml().forEachSubConfiguration { s, configurationSection ->
                    block(it, s, configurationSection)
                }
            }
        } else {
            sender.warn("This is not a yml file: ${it.path}")
        }
    }
}