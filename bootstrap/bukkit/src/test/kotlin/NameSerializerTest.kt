package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.util.LEGACY_SECTION_SERIALIZER
import net.kyori.adventure.text.minimessage.MiniMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NameSerializerTest {

    @Test
    fun plainNameIsUnchanged() {
        assertEquals("Zombie", "Zombie".toMiniMessageString())
    }

    @Test
    fun legacyColorCodeIsConverted() {
        assertEquals("<red>Zombie", "§cZombie".toMiniMessageString())
    }

    @Test
    fun hexColorCodeIsConverted() {
        assertEquals("<#FFD800>Test", "§x§F§F§D§8§0§0Test".toMiniMessageString())
    }

    @Test
    fun miniMessageSpecialCharactersAreEscaped() {
        // MiniMessage escapes the tag-opening '<' ("\\<red>"); a bare '>' is inert to the parser.
        assertEquals("\\<red>", "<red>".toMiniMessageString())
    }

    @Test
    fun miniMessageSpecialCharactersRoundTrip() {
        val input = "<red>"
        val reparsed = MiniMessage.miniMessage().deserialize(input.toMiniMessageString())
        assertEquals(LEGACY_SECTION_SERIALIZER.deserialize(input), reparsed)
    }

    @Test
    fun conversionRoundTripsThroughMiniMessage() {
        val input = "§cCustom §lName"
        val result = input.toMiniMessageString()
        val expected = LEGACY_SECTION_SERIALIZER.deserialize(input)
        assertEquals(expected, MiniMessage.miniMessage().deserialize(result))
    }
}