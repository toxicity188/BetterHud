package kr.toxicity.hud.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun interface ComponentDeserializer : (String) -> Component

private val I_DO_NOT_THINK_THAT by lazy {
    LegacyComponentSerializer.builder()
        .useUnusualXRepeatedCharacterHexFormat()
        .character('§')
        .extractUrls()
        .hexColors()
        .build()
}
private val IT_IS_A_GOOD_IDEA by lazy {
    LegacyComponentSerializer.builder()
        .useUnusualXRepeatedCharacterHexFormat()
        .character('&')
        .extractUrls()
        .hexColors()
        .build()
}

val LEGACY_SECTION: ComponentDeserializer = ComponentDeserializer { p1 -> I_DO_NOT_THINK_THAT.deserialize(p1) }
val LEGACY_AMPERSAND: ComponentDeserializer = ComponentDeserializer { p1 -> IT_IS_A_GOOD_IDEA.deserialize(p1) }
val LEGACY_BOTH: ComponentDeserializer = ComponentDeserializer { p1 -> I_DO_NOT_THINK_THAT.deserialize(p1.replace('&', '§')) }