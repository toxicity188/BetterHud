package kr.toxicity.hud.pack

import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.forEachAsync
import kr.toxicity.hud.util.info
import kr.toxicity.hud.util.subFolder
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.Files
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.pathString

enum class PackType {
    FOLDER {
        override fun generate(byteMap: Map<String, ByteArray>): File {
            if (PackUploader.stop()) info("Resource pack host is stopped.")
            val file = DATA_FOLDER.parentFile.subFolder(ConfigManagerImpl.buildFolderLocation)
            if (byteMap.isSameHash(file)) return file
            val pathMap = run {
                val after = ConfigManagerImpl.buildFolderLocation + File.separatorChar
                Files.walk(file.apply {
                    mkdirs()
                }.toPath()).use { stream ->
                    stream.collect(Collectors.toMap(
                        Function { it.pathString.substringAfter(after) },
                        Function { it.toFile() },
                        { _, b -> b },
                        { TreeMap(Comparator.reverseOrder()) }
                    ))
                }
            }
            fun String.toFile(): File {
                val replace = replace('/', File.separatorChar)
                return synchronized(pathMap) {
                    pathMap.remove(replace)
                } ?: File(file, replace).apply {
                    parentFile.mkdirs()
                }
            }
            byteMap.entries.forEachAsync { (path, bytes) ->
                path.toFile().outputStream().buffered().use {
                    it.write(bytes)
                }
            }
            if (ConfigManagerImpl.clearBuildFolder) pathMap.entries
                .asSequence()
                .filter { (key, _) ->  !key.startsWith("assets/${ConfigManagerImpl.namespace}") }
                .forEach { (_, value) -> value.delete() }
            info("File packed: ${byteMap.values.sumOf { it.size.toLong() }.mbFormat()}")
            return file
        }
    },
    ZIP {
        override fun generate(byteMap: Map<String, ByteArray>): File {
            val file = File(DATA_FOLDER.parentFile, "${ConfigManagerImpl.buildFolderLocation}.zip")
            if (!byteMap.isSameHash(file)) {
                val digest = MessageDigest.getInstance("SHA-1")
                file.outputStream()
                    .buffered()
                    .let { DigestOutputStream(it, digest) }
                    .let { ZipOutputStream(it) }
                    .use { zip ->
                        zip.setComment("BetterHud's resource pack.")
                        zip.setLevel(Deflater.BEST_COMPRESSION)
                        byteMap.forEach {
                            val entry = ZipEntry(it.key)
                            zip.putNextEntry(entry)
                            zip.write(it.value)
                            zip.closeEntry()
                            if (ConfigManagerImpl.enableProtection) {
                                entry.crc = it.value.size.toLong()
                                entry.size = BigInteger(it.value).mod(BigInteger.valueOf(Long.MAX_VALUE)).toLong()
                            }
                        }
                    }
                info("Zip packed: ${file.length().mbFormat()}")
                PackUUID.from(digest).apply {
                    save()
                }
            } else {
                PackUUID.previous
            }?.let { uuid ->
                if (ConfigManagerImpl.enableSelfHost) {
                    PackUploader.upload(uuid, file.inputStream().buffered().use {
                        it.readAllBytes()
                    })
                }
            }
            return file
        }
    },
    NONE {
        override fun generate(byteMap: Map<String, ByteArray>): File? {
            if (PackUploader.stop()) info("Resource pack host is stopped.")
            return null
        }
    },
    ;
    companion object {
        private val decimal = DecimalFormat("#,###.###")

        private fun Long.mbFormat(): String {
            return "${decimal.format(BigDecimal("$this.000") / BigDecimal("1048576.000"))}MB"
        }

        private fun Map<String, ByteArray>.isSameHash(target: File): Boolean {
            return File(DATA_FOLDER.subFolder(".cache"), "zip-hash.txt").run {
                runCatching {
                    MessageDigest.getInstance("SHA-256")
                }.map { digest ->
                    values.forEach {
                        digest.update(it)
                    }
                    UUID.nameUUIDFromBytes(digest.digest()).toString()
                }.getOrNull()?.let {
                    val same = target.exists() && exists() && readText() == it
                    if (!same) writeText(it)
                    same
                }
            } == true
        }
    }

    abstract fun generate(byteMap: Map<String, ByteArray>): File?
}