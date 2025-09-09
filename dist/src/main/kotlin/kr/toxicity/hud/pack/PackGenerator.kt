package kr.toxicity.hud.pack

import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import kr.toxicity.hud.util.forEach
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object PackGenerator {
    private val tasks = TreeMap<String, PackFile>()

    fun generate(info: ReloadInfo): Map<String, ByteArray> {
        val sender = info.sender
        val resourcePack = runCatching {
            var meta = PackMeta.default
            ConfigManagerImpl.mergeOtherFolders.forEach {
                val mergeTarget = DATA_FOLDER.parentFile.subFolder(it)
                when {
                    mergeTarget.isDirectory -> mergeFolder(mergeTarget) { subMeta ->
                        meta += subMeta
                    }
                    mergeTarget.extension == "zip" -> mergeZip(mergeTarget) { subMeta ->
                        meta += subMeta
                    }
                }
            }
            PackOverlay.entries.forEach {
                it.loadAssets()
            }
            addTask(listOf("pack.mcmeta")) {
                meta.toByteArray()
            }
            BOOTSTRAP.resource("icon.png")?.buffered()?.use {
                val read = it.readAllBytes()
                addTask(listOf("pack.png")) {
                    read
                }
            }
            TreeMap<String, ByteArray>().apply {
                tasks.values.forEachAsync {
                    val get = it()
                    synchronized(this) {
                        put(it.path, get)
                    }
                }
            }
        }.getOrElse {
            it.handle(sender, "Unable to make a resource pack.")
            emptyMap()
        }
        tasks.clear()
        return resourcePack
    }

    private fun mergeFolder(mergeTarget: File, metaBlock: (PackMeta) -> Unit) {
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
                    metaBlock(target.toMcmeta())
                }.getOrElse { e ->
                    e.handle("Invalid pack.mcmeta: ${target.path}")
                }
            } else addFile(target)
        }
    }

    private fun mergeZip(mergeTarget: File, metaBlock: (PackMeta) -> Unit) {
        ZipInputStream(mergeTarget.inputStream().buffered()).use {
            var entry: ZipEntry?
            do {
                entry = it.nextEntry
                entry?.let { e ->
                    val read = it.readAllBytes()
                    if (e.name == "pack.mcmeta") {
                        runCatching {
                            metaBlock(read.toMcmeta())
                        }.getOrElse { e ->
                            e.handle("Invalid pack.mcmeta: ${mergeTarget.path}")
                        }
                    } else {
                        addTask(e.name.split("[/,\\\\]".toRegex())) {
                            read
                        }
                    }
                    it.closeEntry()
                }
            } while (entry != null)
        }
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