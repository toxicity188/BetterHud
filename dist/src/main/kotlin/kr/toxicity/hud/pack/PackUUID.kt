package kr.toxicity.hud.pack

import kr.toxicity.hud.util.DATA_FOLDER
import kr.toxicity.hud.util.handle
import kr.toxicity.hud.util.subFolder
import java.io.File
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

data class PackUUID(
    val digest: ByteArray,
    val hash: String,
    val uuid: UUID
) {
    companion object {

        private val cacheFile by lazy {
            File(DATA_FOLDER.subFolder(".cache"), "pack-digest.txt")
        }

        private var _previous: PackUUID? = null
        var previous: PackUUID?
            get() = _previous ?: load?.also { _previous = it }
            private set(value) {
                _previous = value?.apply {
                    cacheFile.run {
                        runCatching {
                            bufferedWriter().use {
                                it.write(Base64.getEncoder().encodeToString(digest) + '\n')
                                it.write(hash + '\n')
                                it.write(uuid.toString())
                            }
                            true
                        }.getOrElse {
                            it.handle("Unable to save pack digest.")
                            false
                        }
                    }
                }
            }

        private val load
            get() = cacheFile.run {
                if (exists()) runCatching {
                    bufferedReader().use {
                        PackUUID(
                            Base64.getDecoder().decode(it.readLine()),
                            it.readLine(),
                            UUID.fromString(it.readLine())
                        )
                    }
                }.getOrElse {
                    it.handle("Unable to load pack digest.")
                    null
                } else null
            }

        fun from(messageDigest: MessageDigest): PackUUID {
            val hash = StringBuilder(40)
            val digest = messageDigest.digest()
            for (element in digest) {
                val byte = element.toInt()
                hash.append(((byte shr 4) and 15).digitToChar(16))
                    .append((byte and 15).digitToChar(16))
            }
            val string = hash.toString()
            var t = 0
            val uuid = UUID.nameUUIDFromBytes(ByteArray(20) {
                ((Character.digit(hash.codePointAt(t++), 16) shl 4) or Character.digit(hash.codePointAt(t++), 16)).toByte()
            })
            return PackUUID(
                digest,
                string,
                uuid
            )
        }
    }

    fun save()  {
        previous = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PackUUID) return false

        if (!digest.contentEquals(other.digest)) return false
        if (hash != other.hash) return false
        if (uuid != other.uuid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = digest.contentHashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + uuid.hashCode()
        return result
    }
}