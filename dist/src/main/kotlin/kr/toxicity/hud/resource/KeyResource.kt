package kr.toxicity.hud.resource

import kr.toxicity.hud.util.encodeKey
import net.kyori.adventure.key.Key

class KeyResource(namespace: String) {
    companion object {
        val space = "space".encodeKey()
        val legacySpace = "legacy_space".encodeKey()
        val default = "default".encodeKey()
        val spaces = "spaces".encodeKey()
        val splitter = "splitter".encodeKey()
    }

    val encodedNamespace = namespace.encodeKey()

    private fun create(name: String) = Key.key(encodedNamespace, "$name/$name")

    val spaceKey = create(space)
    val legacySpaceKey = create(legacySpace)
    val defaultKey = create(default)
    val spacesTtfKey = create(spaces)
    val splitterKey = create(splitter)
}