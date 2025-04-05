package kr.toxicity.hud.pack

import kr.toxicity.hud.api.manager.ConfigManager
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import java.io.File
import java.util.*

object PackGenerator {
    private val tasks = TreeMap<String, PackFile>()
    fun generate(info: ReloadInfo): Map<String, ByteArray> {
        val sender = info.sender
        val resourcePack = runCatching {
            var meta = PackMeta.default
            ConfigManagerImpl.mergeOtherFolders.forEach {
                val mergeTarget = DATA_FOLDER.parentFile.subFolder(it)
                val mergeLength = mergeTarget.path.length + 1
                fun addFile(target: File) {
                    if (target.isDirectory) target.forEach { t ->
                        addFile(t)
                    } else {
                        addTask(target.path.substring(mergeLength).split(File.separatorChar)) {
                            target.inputStream().buffered().use { stream ->
                                stream.readAllBytes()
                            }
                        }
                    }
                }
                mergeTarget.forEach { target ->
                    if (target.name == "pack.mcmeta") {
                        runCatching {
                            meta += PackMeta.fromFile(target)
                        }.getOrElse {
                            it.handle("Invalid pack.mcmeta: ${target.path}")
                        }
                    } else addFile(target)
                }
            }
            runCatching {
                ConfigManagerImpl.packType.createGenerator(meta, info).use { saveTask ->
                    tasks.values.forEachAsync { t ->
                        runCatching {
                            saveTask(t)
                            debug(ConfigManager.DebugLevel.FILE,"Pack file ${t.path} is generated.")
                        }.onFailure {
                            it.handle(sender, "Unable to save this file: ${t.path}")
                        }
                    }
                    saveTask.resourcePack
                }
            }.getOrElse {
                it.handle(sender, "Unable to finalized resource pack build.")
                emptyMap()
            }
        }.getOrElse {
            it.handle(sender, "Unable to make a resource pack.")
            emptyMap()
        }
        tasks.clear()
        return resourcePack
    }

    fun addTask(dir: Iterable<String>, byteArray: () -> ByteArray) {
        val str = dir.joinToString("/")
        synchronized(tasks) {
            tasks.computeIfAbsent(str) {
                PackFile(str, byteArray)
            }
        }
    }
}