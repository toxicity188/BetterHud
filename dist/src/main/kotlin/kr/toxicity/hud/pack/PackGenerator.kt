package kr.toxicity.hud.pack

import com.google.gson.JsonObject
import kr.toxicity.hud.manager.ConfigManager
import kr.toxicity.hud.util.*
import org.bukkit.Bukkit
import java.io.File
import java.math.BigInteger
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.Comparator
import java.util.TreeMap
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PackGenerator {
    private val tasks = TreeMap<String, PackFile>()

    private class ZipBuilder(
        val zip: ZipOutputStream
    ) {
        var byte = 0
    }

    fun generate(callback: () -> Unit) {
        runCatching {
            val saveTask: Generator = when (ConfigManager.packType) {
                PackType.FOLDER -> {
                    val build = DATA_FOLDER.parentFile.subFolder(ConfigManager.buildFolderLocation)
                    val pathLength = build.path.length + 1
                    val locationMap = TreeMap<String, File>(Comparator.reverseOrder())
                    fun getAllLocation(file: File) {
                        locationMap[file.path.substring(pathLength)] = file
                        file.listFiles()?.forEach {
                            getAllLocation(it)
                        }
                    }
                    build.listFiles()?.forEach {
                        getAllLocation(it)
                    }
                    PLUGIN.loadAssets("pack") { a, i ->
                        val replace = a.replace('/','\\')
                        (locationMap.remove(replace) ?: File(build, replace).apply {
                            parentFile.mkdirs()
                        }).outputStream().buffered().use { os ->
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
                    val protection = ConfigManager.enableProtection
                    val host = ConfigManager.enableSelfHost
                    val message = runCatching {
                        MessageDigest.getInstance("SHA-1")
                    }.getOrNull()
                    val file = File(DATA_FOLDER.parentFile, "${ConfigManager.buildFolderLocation}.zip")
                    val stream = file.apply {
                        if (!exists()) delete()
                    }.outputStream().buffered()
                    val zip = ZipBuilder(ZipOutputStream(message?.let {
                        DigestOutputStream(stream, it)
                    } ?: stream).apply {
                        setComment("BetterHud resource pack.")
                        setLevel(Deflater.BEST_COMPRESSION)
                        PLUGIN.loadAssets("pack") { s, i ->
                            putNextEntry(ZipEntry(s.replace('\\','/')))
                            write(i.readAllBytes())
                            closeEntry()
                        }
                    })
                    fun addEntry(entry: ZipEntry, byte: ByteArray) {
                        synchronized(zip) {
                            zip.byte += byte.size
                            zip.zip.putNextEntry(entry)
                            zip.zip.write(byte)
                            zip.zip.closeEntry()
                            if (protection) {
                                entry.crc = byte.size.toLong()
                                entry.size = BigInteger(byte).mod(BigInteger.valueOf(Long.MAX_VALUE)).toLong()
                            }
                        }
                    }
                    if (host) {
                        PLUGIN.getResource("icon.png")?.buffered()?.use {
                            addEntry(ZipEntry("pack.png"), it.readAllBytes())
                        }
                        addEntry(ZipEntry("pack.mcmeta"), JsonObject().apply {
                            add("pack", JsonObject().apply {
                                addProperty("pack_format", PLUGIN.nms.version.metaVersion)
                                addProperty("description", "BetterHud's self host pack.")
                            })
                        }.toByteArray())
                    }
                    object : Generator {
                        override fun close() {
                            synchronized(zip) {
                                zip.zip.close()
                                if (host && message != null) {
                                    PackUploader.upload(message, file.inputStream().buffered().use {
                                        it.readAllBytes()
                                    })
                                } else PackUploader.stop()
                            }
                        }

                        override fun invoke(p1: PackFile) {
                            val entry = ZipEntry(p1.path)
                            val byte = p1.array()
                            addEntry(entry, byte)
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
        }.onFailure { e ->
            warn("Unable to make a resource pack.")
            warn("Reason: ${e.message}")
            callback()
            tasks.clear()
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

    private interface Generator: (PackFile) -> Unit, AutoCloseable
}