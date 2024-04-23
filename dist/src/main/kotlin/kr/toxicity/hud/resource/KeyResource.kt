package kr.toxicity.hud.resource

import kr.toxicity.hud.util.encodeFile
import kr.toxicity.hud.util.encodeKey
import net.kyori.adventure.key.Key

class KeyResource(namespace: String) {
    companion object {
        val space = "space".encodeKey()
        val legacySpace = "legacy_space".encodeKey()
        val default = "default".encodeKey()
        val spaces = "spaces.ttf".encodeFile()
        val splitter = "splitter.png".encodeFile()
    }

    val encodedNamespace = namespace.encodeKey()

    val spaceKey = Key.key(encodedNamespace, space)
    val legacySpaceKey = Key.key(encodedNamespace, legacySpace)
    val defaultKey = Key.key(encodedNamespace, default)
    val spacesTtfKey = Key.key(encodedNamespace, spaces)
    val splitterKey = Key.key(encodedNamespace, splitter)
}