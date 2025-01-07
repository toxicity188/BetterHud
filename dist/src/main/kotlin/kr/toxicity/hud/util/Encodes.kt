package kr.toxicity.hud.util

import kr.toxicity.hud.manager.ConfigManagerImpl
import kr.toxicity.hud.manager.EncodeManager

fun String.encodeFile(namespace: EncodeManager.EncodeNamespace): String {
    val split = split('.')
    if (split.size != 2) throw RuntimeException("Invaild file name: $this")
    return "${split[0].encodeKey(namespace)}.${split[1]}"
}

fun String.encodeKey(namespace: EncodeManager.EncodeNamespace) = if (ConfigManagerImpl.resourcePackObfuscation) EncodeManager.generateKey(namespace, this) else this