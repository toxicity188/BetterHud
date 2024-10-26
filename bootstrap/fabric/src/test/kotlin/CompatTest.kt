import kr.toxicity.hud.api.placeholder.HudPlaceholder
import kr.toxicity.hud.bootstrap.fabric.compatibility.Compatibility
import kr.toxicity.hud.bootstrap.fabric.manager.CompatibilityManager
import kr.toxicity.hud.bootstrap.fabric.module.Module
import kr.toxicity.hud.bootstrap.fabric.module.MODULE_FABRIC
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
            .resolve("placeholders-fabric.md")
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

        println("# Fabric")
        MODULE_FABRIC.forEach {
            it.value().print(it.key)
        }
        println("")

        println("# Compatibility")
        CompatibilityManager.compatibilities.forEach {
            it.value().print(it.key)
        }
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

    private fun Compatibility.print(name: String) {
        val n = name.lowercase().replace('-', '_')

        val mapper = placeholderMapper(n)

        println("## [$name]($website)")
        triggers.keys.printForEach("**Triggers**")
        listeners.keys.printForEach("**Listeners**")
        strings.entries.printForEach("**Placeholders** (String)", mapper)
        numbers.entries.printForEach("**Placeholders** (Number)", mapper)
        booleans.entries.printForEach("**Placeholders** (Boolean)", mapper)
        println("")
    }
}