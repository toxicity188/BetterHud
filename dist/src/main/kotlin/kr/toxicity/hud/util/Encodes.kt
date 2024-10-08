package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl

fun String.encodeFile(): String {
    val split = split('.')
    if (split.size != 2) throw RuntimeException("Invaild file name: $this")
    return "${split[0].encodeKey()}.${split[1]}"
}
fun String.decodeFile(): String {
    val split = split('.')
    if (split.size != 2) throw RuntimeException("Invaild file name: $this")
    return "${split[0].decodeKey()}.${split[1]}"
}

fun String.encodeKey(): String {
    return if (ConfigManagerImpl.resourcePackObfuscation) {
        val sb = StringBuilder()
        fun Int.encode(): Int {
            val and = inv() and 0xF
            return (and + 3) % (0xF + 1)
        }

        fun append(int: Int) {
            sb.append(Character.forDigit(int, 16))
        }
        forEach {
            if (it.code > 0xFF) throw RuntimeException("Invalid name: $this. you must use ([a-zA-Z]|_).")
            val utf8Char = it.code and 0xFF
            append((utf8Char shr 4).encode())
            append(utf8Char.encode())
        }
        sb.toString().reversed()
    } else this
}

fun String.decodeKey(): String {
    return if (ConfigManagerImpl.resourcePackObfuscation) {
        val sb = StringBuilder()
        if (length % 2 != 0) throw RuntimeException("$this isn't encode key.")
        var t = 0
        fun Int.decode(): Int {
            var m = this - 3
            if (m < 0) m += 0xF + 1
            return m.inv() and 0xF
        }

        val reversed = reversed()
        for (i in 0..<length / 2) {
            sb.append(
                ((Character.digit(reversed[t++], 16).decode() shl 4) or
                        Character.digit(reversed[t++], 16).decode()).toChar()
            )
        }
        return sb.toString()
    } else this
}