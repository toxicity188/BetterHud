package kr.toxicity.hud.pack

import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.*
import net.kyori.adventure.audience.Audience
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

object PackGenerator {
    private val tasks = TreeMap<String, PackFile>()

    @Volatile
    private var beforeByte = 0L
    private val decimal = DecimalFormat("#,###.###")

    private fun mbFormat(long: Long): String {
        return "${decimal.format(BigDecimal("${long}.000") / BigDecimal("1048576.000"))}MB"
    }

    private open class Builder {
        @Volatile
        var byte = 0L
        val byteArrayMap = HashMap<String, ByteArray>()
    }
    private class ZipBuilder(
        val zip: ZipOutputStream
    ) : Builder()
    private class FileTreeBuilder(
        private val build: File
    ) : Builder() {
        val locationMap = TreeMap<String, File>(Comparator.reverseOrder())

        fun save(packFile: PackFile) {
            val replace = packFile.path.replace('/', File.separatorChar)
            val arr = packFile.array()
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

    fun generate(sender: Audience): Map<String, ByteArray> {
        val resourcePack = runWithExceptionHandling(sender, "Unable to make a resource pack.") {
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
                    addFile(target)
                }
            }
            val saveTask: Generator = when (ConfigManagerImpl.packType) {
                PackType.NONE -> {
                    val builder = Builder()
                    object : Generator {
                        override val resourcePack: Map<String, ByteArray>
                            get() = Collections.unmodifiableMap(builder.byteArrayMap)

                        override fun close() {
                            if (PackUploader.stop()) info("Resource pack host is stopped.")
                        }

                        override fun invoke(p1: PackFile) {
                            val byte = p1.array()
                            synchronized(builder) {
                                builder.byte += byte.size
                                builder.byteArrayMap[p1.path] = byte
                            }
                        }
                    }
                }
                PackType.FOLDER -> {
                    val build = DATA_FOLDER.parentFile.subFolder(ConfigManagerImpl.buildFolderLocation)
                    val pathLength = build.path.length + 1
                    val builder = FileTreeBuilder(build)
                    fun getAllLocation(file: File, length: Int) {
                        builder.locationMap.put(file.path.substring(length), file)?.let {
                            warn("Duplicated file skipped: ${file.path} and ${it.path}")
                        }
                        file.forEach {
                            getAllLocation(it, length)
                        }
                    }
                    build.forEach {
                        getAllLocation(it, pathLength)
                    }
                    object : Generator {
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
                PackType.ZIP -> {
                    val protection = ConfigManagerImpl.enableProtection
                    val host = ConfigManagerImpl.enableSelfHost
                    val message = runCatching {
                        MessageDigest.getInstance("SHA-1")
                    }.getOrNull()
                    val file = File(DATA_FOLDER.parentFile, "${ConfigManagerImpl.buildFolderLocation}.zip")
                    val stream = file.outputStream().buffered()
                    val zip = ZipBuilder(ZipOutputStream(message?.let {
                        DigestOutputStream(stream, it)
                    } ?: stream).apply {
                        setComment("BetterHud resource pack.")
                        setLevel(Deflater.BEST_COMPRESSION)
                    })
                    fun addEntry(entry: ZipEntry, byte: ByteArray) {
                        synchronized(zip) {
                            zip.byteArrayMap[entry.name] = byte
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
                        BOOTSTRAP.resource("icon.png")?.buffered()?.use {
                            addEntry(ZipEntry("pack.png"), it.readAllBytes())
                        }
                        addEntry(ZipEntry("pack.mcmeta"), jsonObjectOf(
                            "pack" to jsonObjectOf(
                                "pack_format" to BOOTSTRAP.mcmetaVersion(),
                                "description" to "BetterHud's self-host pack."
                            )
                        ).toByteArray())
                    }
                    object : Generator {
                        override val resourcePack: Map<String, ByteArray>
                            get() = Collections.unmodifiableMap(zip.byteArrayMap)

                        override fun close() {
                            synchronized(zip) {
                                zip.zip.close()
                                info(
                                    "File packed: ${if (beforeByte > 0) "${mbFormat(beforeByte)} -> ${mbFormat(zip.byte)}" else mbFormat(zip.byte)}",
                                    "File zipped: ${mbFormat(file.length())}"
                                )
                                if (ConfigManagerImpl.needToUpdatePack || ConfigManagerImpl.forceUpdate || beforeByte != zip.byte) {
                                    beforeByte = zip.byte
                                    if (host && message != null) {
                                        PackUploader.upload(message, file.inputStream().buffered().use {
                                            it.readAllBytes()
                                        })
                                    } else PackUploader.stop()
                                }
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
            tasks.values.forEachAsync { t ->
                runWithExceptionHandling(sender, "Unable to save this file: ${t.path}") {
                    saveTask(t)
                }
            }
            runWithExceptionHandling(sender, "Unable to finalized resource pack build.") {
                saveTask.close()
            }
            tasks.clear()
            saveTask.resourcePack
        }.getOrElse {
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

    private interface Generator : (PackFile) -> Unit, AutoCloseable {
        val resourcePack: Map<String, ByteArray>
    }
}