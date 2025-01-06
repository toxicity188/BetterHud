import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.attributes
import java.io.File
import java.time.LocalDateTime

val buildNumber: String? = System.getenv("BUILD_NUMBER")

val SUPPORTED_MINECRAFT_VERSION = listOf(
    //1.19
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4",
    //1.20
    "1.20",
    "1.20.1",
    "1.20.2",
    "1.20.3",
    "1.20.4",
    "1.20.5",
    "1.20.6",
    //1.21
    "1.21",
    "1.21.1",
    "1.21.2",
    "1.21.3",
    "1.21.4"
)

val Project.libs
    get() = rootProject.extensions.getByName("libs") as LibrariesForLibs

fun Jar.setManifest() {
    manifest {
        attributes(
            "Dev-Build" to (buildNumber != null),
            "Version" to project.version,
            "Author" to "toxicity188",
            "Url" to "https://github.com/toxicity188/BetterHud",
            "Created-By" to "Gradle ${project.gradle.gradleVersion}",
            "Build-Jdk" to "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}",
            "Build-OS" to "${System.getProperty("os.arch")} ${System.getProperty("os.name")}",
            "Build-Date" to LocalDateTime.now().toString()
        )
    }
}

fun Jar.relocateAll() {
    val file = archiveFile.get().asFile
    val tempFile = file.copyTo(File.createTempFile("jar-relocator", System.currentTimeMillis().toString()).apply {
        if (exists()) delete()
    })
    JarRelocator(
        tempFile,
        file,
        listOf(
            "kotlin",
            "net.objecthunter.exp4j",
            "net.jodah.expiringmap",
            "org.bstats",
            "me.lucko.jarrelocator",
            "kr.toxicity.command.impl"
        ).map {
            Relocation(it, "${project.group}.shaded.$it")
        }
    ).run()
    tempFile.delete()
}