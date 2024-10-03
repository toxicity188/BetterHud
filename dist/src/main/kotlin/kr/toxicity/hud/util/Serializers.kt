package kr.toxicity.hud.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

interface ComponentDeserializer: (String) -> Component

val LEGACY_SECTION: ComponentDeserializer = object : ComponentDeserializer {
    override fun invoke(p1: String): Component = LegacyComponentSerializer.legacySection().deserialize(p1)
}
val LEGACY_AMPERSAND: ComponentDeserializer = object : ComponentDeserializer {
    override fun invoke(p1: String): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(p1)
}
val LEGACY_BOTH: ComponentDeserializer = object : ComponentDeserializer {
    override fun invoke(p1: String): Component = LegacyComponentSerializer.legacySection().deserialize(p1.replace('&', '§'))
}