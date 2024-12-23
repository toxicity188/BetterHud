package kr.toxicity.hud.pack

import kr.toxicity.hud.api.plugin.ReloadFlagType
import kr.toxicity.hud.api.plugin.ReloadInfo
import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.*
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class PackType {
    FOLDER {
        @Volatile
        private var beforeByte = 0L
        private inner class FileTreeBuilder(
            private val build: File
        ) : Builder() {
            val locationMap = TreeMap<String, File>(Comparator.reverseOrder())

            fun save(packFile: PackFile) {
                val replace = packFile.path.replace('/', File.separatorChar)
                val arr = packFile()
                synchronized(this) {
                    byte += arr.size
                    byteArrayMap[packFile.path] = arr
                }
                (synchronized(locationMap) {
                    locationMap.remove(replace)
                } ?: File(build, replace).apply {
                    parentFile.mkdirs()
                }).outputStream().buffered().use { stream ->
                    stream.write(arr)
                }
            }
            fun close() {
                synchronized(this) {
                    if (ConfigManagerImpl.clearBuildFolder) {
                        val iterator = locationMap.values.iterator()
                        synchronized(iterator) {
                            while (iterator.hasNext()) {
                                val next = iterator.next()
                                if (next.listFiles()?.isNotEmpty() == true) continue
                                next.delete()
                            }
                        }
                    }
                    info("File packed: ${if (beforeByte > 0) "${mbFormat(beforeByte)} -> ${mbFormat(byte)}" else mbFormat(byte)}")
                    if (beforeByte != byte) {
                        beforeByte = byte
                    }
                }
            }
        }

        override fun createGenerator(info: ReloadInfo): Generator {
            val build = DATA_FOLDER.parentFile.subFolder(ConfigManagerImpl.buildFolderLocation)
            val pathLength = build.path.length + 1
            val builder = FileTreeBuilder(build)
            fun getAllLocation(file: File, length: Int) {
                builder.locationMap.put(file.path.substring(length), file)?.let {
                    info.sender.warn("Duplicated file skipped: ${file.path} and ${it.path}")
                }
                file.forEach {
                    getAllLocation(it, length)
                }
            }
            build.forEach {
                getAllLocation(it, pathLength)
            }
            return object : Generator {
                override val resourcePack: Map<String, ByteArray>
                    get() = Collections.unmodifiableMap(builder.byteArrayMap)

                override fun close() {
                    if (PackUploader.stop()) info("Resource pack host is stopped.")
                    builder.close()
                }
                override fun invoke(p1: PackFile) {
                    builder.save(p1)
                }
            }
        }
    },
    ZIP {
        @Volatile
        private var beforeByte = 0L

        private inner class ZipBuilder(
            val zip: ZipOutputStream
        ) : Builder()

        override fun createGenerator(info: ReloadInfo): Generator {
            val protection = ConfigManagerImpl.enableProtection
            val host = ConfigManagerImpl.enableSelfHost
            val message = runCatching {
                MessageDigest.getInstance("SHA-1")
            }.getOrNull()
            val file = File(DATA_FOLDER.parentFile, "${ConfigManagerImpl.buildFolderLocation}.zip")
            beforeByte = file.length()
            val stream = ByteArrayOutputStream()
            val zip = ZipBuilder(ZipOutputStream(stream.buffered()).apply {
                setComment("BetterHud resource pack.")
                setLevel(Deflater.BEST_COMPRESSION)
            })
            fun addEntry(entry: ZipEntry, byte: ByteArray) {
                synchronized(zip) {
                    runWithExceptionHandling(info.sender, "Unable to write this file: ${entry.name}") {
                        zip.byteArrayMap[entry.name] = byte
                        zip.zip.putNextEntry(entry)
                        zip.zip.write(byte)
                        zip.zip.closeEntry()
                        if (protection) {
                            entry.crc = byte.size.toLong()
                            entry.size = BigInteger(byte).mod(BigInteger.valueOf(Long.MAX_VALUE)).toLong()
                        }
                    }
                }
            }
            if (host) {
                BOOTSTRAP.resource("icon.png")?.buffered()?.use {
                    addEntry(ZipEntry("pack.png"), it.readAllBytes())
                }
                addEntry(
                    ZipEntry("pack.mcmeta"), jsonObjectOf(
                    "pack" to jsonObjectOf(
                        "pack_format" to BOOTSTRAP.mcmetaVersion(),
                        "description" to "BetterHud's self-host pack."
                    )
                ).toByteArray())
            }
            return object : Generator {
                override val resourcePack: Map<String, ByteArray>
                    get() = Collections.unmodifiableMap(zip.byteArrayMap)

                override fun close() {
                    synchronized(zip) {
                        zip.zip.close()
                        val finalByte = stream.toByteArray()
                        info(
                                "File packed: ${if (beforeByte > 0) "${mbFormat(beforeByte)} -> ${mbFormat(finalByte.size.toLong())}" else mbFormat(finalByte.size.toLong())}",
                        )
                        if (message == null || !host) return
                        val previousUUID = PackUUID.previous
                        if (previousUUID == null || ConfigManagerImpl.forceUpdate || beforeByte != finalByte.size.toLong() || info.has(ReloadFlagType.FORCE_GENERATE_RESOURCE_PACK)) {
                            beforeByte = finalByte.size.toLong()
                            DigestOutputStream(file.outputStream(), message).buffered().use {
                                it.write(finalByte)
                            }
                            val uuid = PackUUID.from(message)
                            info(
                                "File zipped: ${mbFormat(file.length())}"
                            )
                            PackUploader.upload(uuid, file.inputStream().buffered().use {
                                it.readAllBytes()
                            })
                        } else {
                            PackUploader.upload(previousUUID , file.inputStream().buffered().use {
                                it.readAllBytes()
                            })
                        }
                    }
                }

                override fun invoke(p1: PackFile) {
                    val entry = ZipEntry(p1.path)
                    val byte = p1()
                    addEntry(entry, byte)
                }
            }
        }
    },
    NONE {
        override fun createGenerator(info: ReloadInfo): Generator {
            val builder = Builder()
            return object : Generator {
                override val resourcePack: Map<String, ByteArray>
                    get() = Collections.unmodifiableMap(builder.byteArrayMap)

                override fun close() {
                    if (PackUploader.stop()) info("Resource pack host is stopped.")
                }

                override fun invoke(p1: PackFile) {
                    val byte = p1()
                    synchronized(builder) {
                        builder.byte += byte.size
                        builder.byteArrayMap[p1.path] = byte
                    }
                }
            }
        }
    },
    ;
    companion object {
        private val decimal = DecimalFormat("#,###.###")

        private fun mbFormat(long: Long): String {
            return "${decimal.format(BigDecimal("${long}.000") / BigDecimal("1048576.000"))}MB"
        }
    }

    private open class Builder {
        @Volatile
        var byte = 0L
        val byteArrayMap = HashMap<String, ByteArray>()
    }
    
    abstract fun createGenerator(info: ReloadInfo): Generator
    
    interface Generator : (PackFile) -> Unit, AutoCloseable {
        val resourcePack: Map<String, ByteArray>
    }
}