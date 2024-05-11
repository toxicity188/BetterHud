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


fun File.forEachAsync(block: (File) -> Unit) {
    listFiles()?.toList()?.forEachAsync(block)
}

fun File.forEachAllYaml(block: (File, String, ConfigurationSection) -> Unit) {
    forEachAllFolder {
        if (it.extension == "yml") {
            runWithExceptionHandling("Unable to load this yml file: ${it.name}") {
                it.toYaml().forEachSubConfiguration { s, configurationSection ->
                    block(it, s, configurationSection)
                }
            }
        }
    }
}
fun File.forEachAllYamlAsync(block: (File, String, ConfigurationSection) -> Unit) {
    fun getAll(file: File): List<File> {
        return if (file.isDirectory) {
            file.listFiles()?.map { subFile ->
                getAll(subFile)
            }?.sum() ?: ArrayList()
        } else {
            listOf(file)
        }
    }
    val list = getAll(this).filter {
        it.extension == "yml"
    }.mapNotNull {
        runCatching {
            val yaml = it.toYaml()
            val list = ArrayList<Pair<String, ConfigurationSection>>()
            yaml.getKeys(false).forEach {
                yaml.getConfigurationSection(it)?.let { section ->
                    list.add(it to section)
                }
            }
            if (list.isNotEmpty()) it to list else null
        }.getOrElse { e ->
            warn(
                "Unable to load this yml file: ${it.name}",
                "Reason: ${e.message}"
            )
            null
        }
    }
    if (list.isEmpty()) {
        return
    }
    list.map {
        {
            it.second.forEach { pair ->
                block(it.first, pair.first, pair.second)
            }
        }
    }.forEachAsync {
        it()
    }
}