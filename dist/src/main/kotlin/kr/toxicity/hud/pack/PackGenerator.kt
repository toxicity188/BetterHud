package kr.toxicity.hud.pack

import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.util.*
import java.io.File
import java.util.Comparator
import java.util.TreeMap
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PackGenerator {
    private val tasks = TreeMap<String, PackFile>()

    fun generate(callback: () -> Unit) {
        val saveTask: Generator = when (ConfigManager.packType) {
            PackType.FOLDER -> {
                val build = DATA_FOLDER.parentFile.subFolder(ConfigManager.buildFolderLocation)
                val pathLength = build.path.length + 1
                val locationMap = TreeMap<String, File>(Comparator.reverseOrder())
                fun getAllLocation(file: File) {
                    locationMap[file.path.substring(pathLength, file.path.length)] = file
                    file.listFiles()?.forEach {
                        getAllLocation(it)
                    }
                }
                build.listFiles()?.forEach {
                    getAllLocation(it)
                }
                PLUGIN.loadAssets("pack") { a, i ->
                    val replace = a.replace('/','\\')
                    (locationMap.remove(replace) ?: File(build, replace)).outputStream().buffered().use { os ->
                        i.copyTo(os)
                    }
                }
                object : Generator {
                    override fun close() {
                        synchronized(locationMap) {
                            val iterator = locationMap.values.iterator()
                            synchronized(iterator) {
                                while (iterator.hasNext()) {
                                    val next = iterator.next()
                                    if (next.listFiles()?.isNotEmpty() == true) continue
                                    next.delete()
                                }
                            }
                        }
                    }
                    override fun invoke(p1: PackFile) {
                        val replace = p1.path.replace('/','\\')
                        (synchronized(locationMap) {
                            locationMap.remove(replace)
                        } ?: File(build, replace).apply {
                            parentFile.mkdirs()
                        }).outputStream().buffered().use { stream ->
                            stream.write(p1.array())
                        }
                    }
                }
            }
            PackType.ZIP -> {
                val zip = ZipOutputStream(File(DATA_FOLDER.parentFile, "${ConfigManager.buildFolderLocation}.zip").apply {
                    if (!exists()) delete()
                }.outputStream().buffered()).apply {
                    setComment("BetterHud resource pack.")
                    setLevel(Deflater.BEST_COMPRESSION)
                    PLUGIN.loadAssets("pack") { s, i ->
                        putNextEntry(ZipEntry(s.replace('\\','/')))
                        write(i.readAllBytes())
                        closeEntry()
                    }
                }
                object : Generator {
                    override fun close() {
                        synchronized(zip) {
                            zip.close()
                        }
                    }

                    override fun invoke(p1: PackFile) {
                        val entry = ZipEntry(p1.path)
                        val byte = p1.array()
                        synchronized(zip) {
                            zip.putNextEntry(entry)
                            zip.write(byte)
                            zip.closeEntry()
                        }
                    }
                }
            }
        }
        tasks.values.forEachAsync({ t ->
            runCatching {
                saveTask(t)
            }.onFailure { e ->
                warn("Unable to save this file: ${t.path}")
                warn("Reason: ${e.message}")
            }
        }) {
            runCatching {
                saveTask.close()
            }.onFailure { e ->
                warn("Unable to finalized resource pack build.")
                warn("Reason: ${e.message}")
            }
            callback()
            tasks.clear()
        }
    }

    fun addTask(dir: Iterable<String>, byteArray: () -> ByteArray) {
        val str = dir.joinToString("/")
        tasks.computeIfAbsent(str) {
            PackFile(str, byteArray)
        }
    }

    private interface Generator: (PackFile) -> Unit, AutoCloseable
}