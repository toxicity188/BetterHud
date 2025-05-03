import kr.toxicity.hud.api.version.MinecraftVersion
import kr.toxicity.hud.util.latestVersion
import kotlin.test.Test

class ModrinthTest {
    @Test
    fun testModrinth() {
        val latest = latestVersion(
            MinecraftVersion.LATEST,
            "paper"
        )
        println("Release: ${latest.release?.versionNumber}")
        println("Snapshot: ${latest.snapshot?.versionNumber}")
    }
}