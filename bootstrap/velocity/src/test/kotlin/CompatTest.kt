import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.bootstrap.velocity.module.Module
import kr.toxicity.hud.bootstrap.velocity.module.MODULE_VELOCITY
import kr.toxicity.hud.manager.ListenerManagerImpl
import kr.toxicity.hud.manager.PlaceholderManagerImpl
import kr.toxicity.hud.manager.TriggerManagerImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStream
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.io.path.outputStream

class CompatTest {

    private fun placeholderMapper(n: String? = null) = { entry: Map.Entry<String, HudPlaceholder<*>> ->
        val length = entry.value.requiredArgsLength
        buildString {
            append(if (n != null) "**${n}_${entry.key}**" else "**${entry.key}**")
            if (length > 0) {
                append(':')
                append(if (length == 1) "arg" else (0..<length).joinToString(",") { i ->
                    "args${i + 1}"
                })
            }
        }
    }

    private fun <T> Collection<T>.printForEach(name: String, stringMapper: (T) -> String = {
        if (it is String) it else it.toString()
    }) {
        if (isNotEmpty()) {
            println("### $name")
            forEach {
                println(" - ${stringMapper(it)}")
            }
        }
    }

    private lateinit var stream: PrintStream
    private lateinit var output: OutputStream

    @BeforeEach
    fun setup() {
        stream = System.out
        output = Path(System.getProperty("user.dir").substringBefore("bootstrap"))
            .resolve("build")
            .resolve("placeholders-velocity.md")
            .outputStream()
            .buffered().apply {
                System.setOut(PrintStream(this))
            }
    }
    @AfterEach
    fun end() {
        System.setOut(stream)
        output.close()
    }

    @Test
    fun compatTest() {
        val mapper = placeholderMapper()

        println("# Default")
        TriggerManagerImpl.allTriggerKeys.printForEach("**Triggers**")
        ListenerManagerImpl.allListenerKeys.printForEach("**Listeners**")
        PlaceholderManagerImpl.stringContainer.allPlaceholders.entries.printForEach("**Placeholders** (String)", mapper)
        PlaceholderManagerImpl.numberContainer.allPlaceholders.entries.printForEach("**Placeholders** (Number)", mapper)
        PlaceholderManagerImpl.booleanContainer.allPlaceholders.entries.printForEach("**Placeholders** (Boolean)", mapper)
        println("")

        println("# Velocity")
        MODULE_VELOCITY.forEach {
            it.value().print(it.key)
        }
        println("")
    }

    private fun Module.print(name: String) {
        val mapper = if (name == "standard") placeholderMapper() else placeholderMapper(name)

        println("## $name")
        triggers.keys.printForEach("**Triggers**")
        listeners.keys.printForEach("**Listeners**")
        strings.entries.printForEach("**Placeholders** (String)", mapper)
        numbers.entries.printForEach("**Placeholders** (Number)", mapper)
        booleans.entries.printForEach("**Placeholders** (Boolean)", mapper)
        println("")
    }
}