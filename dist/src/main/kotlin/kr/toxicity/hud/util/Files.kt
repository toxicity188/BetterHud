package kr.toxicity.hud.util

import org.bukkit.configuration.ConfigurationSection
import java.io.File
import java.util.concurrent.CompletableFuture

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


fun File.forEachAsync(block: (File) -> Unit, callback: () -> Unit) {
    listFiles()?.toList()?.forEachAsync(block, callback) ?: return callback()
}

fun File.forEachAllYaml(block: (File, String, ConfigurationSection) -> Unit) {
    forEachAllFolder {
        if (it.extension == "yml") {
            runCatching {
                it.toYaml().forEachSubConfiguration { s, configurationSection ->
                    block(it, s, configurationSection)
                }
            }.onFailure { e ->
                warn(
                    "Unable to load this yml file: ${it.name}",
                    "Reason: ${e.message}"
                )
            }
        }
    }
}
fun File.forEachAllYamlAsync(block: (File, String, ConfigurationSection) -> Unit, callback: () -> Unit) {
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
        callback()
        return
    }
    val index = TaskIndex(list.sumOf {
        it.second.size
    })
    if (index.max == 0) {
        callback()
        return
    }
    list.forEach {
        it.second.forEach { pair ->
            CompletableFuture.runAsync {
                runCatching {
                    block(it.first, pair.first, pair.second)
                }.onFailure { e ->
                    e.printStackTrace()
                }
                synchronized(index) {
                    if (++index.current == index.max) {
                        callback()
                    }
                }
            }.handle { _, e ->
                e.printStackTrace()
            }
        }
    }
}