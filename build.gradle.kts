import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    alias(libs.plugins.standardConvention)
    id("com.modrinth.minotaur")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.github.ben-manes.versions") version "0.51.0"
}

dependencies {
    fun searchAll(target: Project) {
        val sub = target.subprojects
        if (sub.isNotEmpty()) sub.forEach {
            searchAll(it)
        } else dokka(target)
    }
    searchAll(rootProject)
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveClassifier = "sources"
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

tasks.register("modrinthPublish") {
    dependsOn(tasks.build)
    finalizedBy(
        tasks.modrinthSyncBody,
        bukkit.tasks.modrinth,
        fabric.tasks.modrinth,
        velocity.tasks.modrinth
    )
}

tasks {
    runServer {
        version(project.property("minecraft_version")!!.toString())
        pluginJars(bukkit.tasks.jar.flatMap {
            it.archiveFile
        })
        pluginJars(fileTree("plugins"))
        downloadPlugins {
            hangar("ViaVersion", "5.2.1")
            hangar("ViaBackwards", "5.2.1")
            hangar("PlaceholderAPI", "2.11.6")
            hangar("Skript", "2.9.5")
        }
    }
    build {
        dependsOn(
            bukkit.tasks.build,
            fabric.tasks.build,
            velocity.tasks.build
        )
        finalizedBy(
            sourcesJar,
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
                platformVersions = listOf("3.3", "3.4")
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_API_TOKEN")
    projectId = "betterhud2"
    syncBodyFrom = rootProject.file("BANNER.md").readText()
}