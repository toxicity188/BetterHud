package kr.toxicity.hud.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun interface ComponentDeserializer : (String) -> Component

val LEGACY_SECTION_SERIALIZER by lazy { //I don't think that
    LegacyComponentSerializer.builder()
        .useUnusualXRepeatedCharacterHexFormat()
        .character('§')
        .extractUrls()
        .hexColors()
        .build()
}
val LEGACY_AMPERSAND_SERIALIZER by lazy { //It is a good idea
    LegacyComponentSerializer.builder()
        .useUnusualXRepeatedCharacterHexFormat()
        .character('&')
        .extractUrls()
        .hexColors()
        .build()
}

val LEGACY_SECTION: ComponentDeserializer = ComponentDeserializer { p1 -> LEGACY_SECTION_SERIALIZER.deserialize(p1) }
val LEGACY_AMPERSAND: ComponentDeserializer = ComponentDeserializer { p1 -> LEGACY_AMPERSAND_SERIALIZER.deserialize(p1) }
val LEGACY_BOTH: ComponentDeserializer = ComponentDeserializer { p1 -> LEGACY_SECTION_SERIALIZER.deserialize(p1.replace('&', '§')) }