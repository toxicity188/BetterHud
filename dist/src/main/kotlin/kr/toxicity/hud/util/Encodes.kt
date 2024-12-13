package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.EncodeManager

fun String.encodeFile(namespace: EncodeManager.EncodeNamespace): String {
    val split = split('.')
    if (split.size != 2) throw RuntimeException("Invaild file name: $this")
    return "${split[0].encodeKey(namespace)}.${split[1]}"
}
fun String.decodeFile(): String {
    val split = split('.')
    if (split.size != 2) throw RuntimeException("Invaild file name: $this")
    return "${split[0].decodeKey()}.${split[1]}"
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

fun String.encodeKey(namespace: EncodeManager.EncodeNamespace) = if (ConfigManagerImpl.resourcePackObfuscation) EncodeManager.generateKey(namespace, this) else this