import kr.toxicity.hud.util.split
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.junit.jupiter.api.Test

class AdventureTest {
    @Test
    fun testAdventure() {
        val serializer = GsonComponentSerializer.gson()
        val message = MiniMessage.miniMessage().deserialize("<gradient:#FF0000:#FFD800>Hello <#FFAC00>world!")
//        val message = Component.text()
//            .content("He")
//            .color(NamedTextColor.GOLD)
//            .append(
//                Component.text()
//                    .content("llo")
//                    .color(NamedTextColor.RED)
//                    .append(
//                        Component.text()
//                            .content(" wor")
//                            .color(NamedTextColor.AQUA)
//                    )
//            )
//            .append(
//                Component.text()
//                    .content("ld!")
//                    .color(NamedTextColor.YELLOW)
//            )
//            .build()

        println("# Original")
        println(serializer.serializeToTree(message))
        println("# Split")
        message.split(20, mapOf(
            'H'.code to 5,
            'e'.code to 5,
            'l'.code to 1,
            'o'.code to 5,
            'w'.code to 5,
            'r'.code to 5,
            'd'.code to 5
        )).forEach {
            println(serializer.serializeToTree(it))
        }
    }
}