import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    alias(libs.plugins.conventions.standard)
    id("com.modrinth.minotaur")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
}

val minecraft = property("minecraft_version")!!.toString()

dependencies {
    fun searchAll(target: Project) {
        val sub = target.subprojects
        if (sub.isNotEmpty()) sub.forEach {
            searchAll(it)
        } else dokka(target)
    }
    searchAll(rootProject)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGenerate)
    archiveClassifier = "javadoc"
    from(layout.buildDirectory.dir("dokka/html").orNull?.asFile)
}

runPaper {
    disablePluginJarDetection()
}

val bukkit = project("bootstrap:bukkit")
val fabric = project("bootstrap:fabric")
val velocity = project("bootstrap:velocity")

tasks.register("pluginJar") {
    dependsOn(bukkit.tasks.build)
}
tasks.register("fabricJar") {
    dependsOn(fabric.tasks.build)
}
tasks.register("velocityJar") {
    dependsOn(velocity.tasks.build)
}

tasks.register("modrinthPublish") {
    finalizedBy(
        tasks.modrinthSyncBody,
        bukkit.tasks.modrinth,
        fabric.tasks.modrinth,
        velocity.tasks.modrinth
    )
}

tasks {
    runServer {
        version(minecraft)
        pluginJars(bukkit.tasks.named<Jar>("shadowJar").flatMap {
            it.archiveFile
        })
        pluginJars(fileTree("plugins"))
        downloadPlugins {
            hangar("ViaVersion", "5.8.1")
            hangar("ViaBackwards", "5.8.1")
            hangar("PlaceholderAPI", "2.12.2")
            hangar("Skript", "2.14.2")
        }
    }
    build {
        dependsOn(
            bukkit.tasks.build,
            fabric.tasks.build,
            velocity.tasks.build
        )
        finalizedBy(
            javadocJar
        )
    }
}

hangarPublish {
    publications.register("plugin") {
        version = project.version as String
        id = "BetterHud"
        apiKey = System.getenv("HANGAR_API_TOKEN")
        val log = System.getenv("COMMIT_MESSAGE")
        if (log != null) {
            changelog = log
            channel = "Snapshot"
        } else {
            changelog = rootProject.file("changelog/${project.version}.md").readText()
            channel = "Release"
        }
        platforms {
            register(Platforms.PAPER) {
                jar = file("build/libs/${project.name}-bukkit-${project.version}.jar")
                platformVersions = SUPPORTED_MINECRAFT_VERSION
            }
            register(Platforms.VELOCITY) {
                jar = file("build/libs/${project.name}-velocity-${project.version}.jar")
                platformVersions = listOf("3.3", "3.4", "3.5")
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_API_TOKEN")
    projectId = "betterhud2"
    syncBodyFrom = rootProject.file("BANNER.md").readText()
}