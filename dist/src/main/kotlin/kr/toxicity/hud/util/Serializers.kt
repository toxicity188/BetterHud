package kr.toxicity.hud.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

fun interface ComponentDeserializer : (String) -> Component

val LEGACY_SECTION: ComponentDeserializer = ComponentDeserializer { p1 -> LegacyComponentSerializer.legacySection().deserialize(p1) }
val LEGACY_AMPERSAND: ComponentDeserializer = ComponentDeserializer { p1 -> LegacyComponentSerializer.legacyAmpersand().deserialize(p1) }
val LEGACY_BOTH: ComponentDeserializer = ComponentDeserializer { p1 -> LegacyComponentSerializer.legacySection().deserialize(p1.replace('&', '§')) }