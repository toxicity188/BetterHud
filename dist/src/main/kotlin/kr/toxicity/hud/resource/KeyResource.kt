package kr.toxicity.hud.resource

import kr.toxicity.hud.util.encodeKey
import net.kyori.adventure.key.Key

class KeyResource(namespace: String) {
    val encodedNamespace = namespace.encodeKey()

    private fun create(name: String) = Key.key(encodedNamespace, name)

    val spaceKey = create("space".encodeKey())
    val legacySpaceKey = create("legacy_space".encodeKey())
    val defaultKey = create("default".encodeKey())
    val spacesTtfKey = create("spaces".encodeKey())
    val splitterKey = create("splitter".encodeKey())
}